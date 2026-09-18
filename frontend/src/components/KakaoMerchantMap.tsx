"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import type { Merchant } from "@/types/contracts";

interface KakaoLatLng {}
interface KakaoMarker {}
interface KakaoLatLngBounds {
  extend(position: KakaoLatLng): void;
}
interface KakaoMapInstance {
  setBounds(bounds: KakaoLatLngBounds): void;
}
interface KakaoMaps {
  load(callback: () => void): void;
  LatLng: new (latitude: number, longitude: number) => KakaoLatLng;
  LatLngBounds: new () => KakaoLatLngBounds;
  Map: new (container: HTMLElement, options: { center: KakaoLatLng; level: number }) => KakaoMapInstance;
  Marker: new (options: { map: KakaoMapInstance; position: KakaoLatLng; title: string }) => KakaoMarker;
  event: { addListener(target: KakaoMarker, event: "click", handler: () => void): void };
}

declare global {
  interface Window {
    kakao?: { maps: KakaoMaps };
  }
}

let sdkPromise: Promise<KakaoMaps> | null = null;

function loadSdk(apiKey: string): Promise<KakaoMaps> {
  if (window.kakao?.maps) {
    return new Promise((resolve) => window.kakao?.maps.load(() => resolve(window.kakao!.maps)));
  }
  if (sdkPromise) return sdkPromise;

  sdkPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(apiKey)}&autoload=false`;
    script.async = true;
    script.onload = () => {
      if (!window.kakao?.maps) {
        reject(new Error("Kakao Map SDK 초기화에 실패했습니다."));
        return;
      }
      window.kakao.maps.load(() => resolve(window.kakao!.maps));
    };
    script.onerror = () => reject(new Error("Kakao Map SDK를 불러오지 못했습니다."));
    document.head.appendChild(script);
  });
  return sdkPromise;
}

type Props = {
  merchants: Merchant[];
  selectedMerchantId: number | null;
  onSelect: (merchant: Merchant) => void;
};

export default function KakaoMerchantMap({ merchants, selectedMerchantId, onSelect }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const [error, setError] = useState<string | null>(null);
  const apiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_API_KEY;
  const mappedMerchants = useMemo(
    () => merchants.filter((merchant) => merchant.latitude != null && merchant.longitude != null),
    [merchants]
  );

  useEffect(() => {
    let active = true;
    if (!apiKey || apiKey.startsWith("replace-with")) {
      setError("지도 API 키를 설정하면 사용처 위치를 확인할 수 있습니다.");
      return;
    }
    if (!containerRef.current || mappedMerchants.length === 0) return;

    setError(null);
    void loadSdk(apiKey).then((maps) => {
      if (!active || !containerRef.current) return;
      const selected = mappedMerchants.find((merchant) => merchant.id === selectedMerchantId);
      const centerMerchant = selected ?? mappedMerchants[0];
      const map = new maps.Map(containerRef.current, {
        center: new maps.LatLng(centerMerchant.latitude!, centerMerchant.longitude!),
        level: 7
      });
      const bounds = new maps.LatLngBounds();
      mappedMerchants.forEach((merchant) => {
        const position = new maps.LatLng(merchant.latitude!, merchant.longitude!);
        const marker = new maps.Marker({ map, position, title: merchant.name });
        maps.event.addListener(marker, "click", () => onSelect(merchant));
        bounds.extend(position);
      });
      map.setBounds(bounds);
    }).catch((sdkError: unknown) => {
      if (active) setError(sdkError instanceof Error ? sdkError.message : "지도를 표시하지 못했습니다.");
    });
    return () => { active = false; };
  }, [apiKey, mappedMerchants, onSelect, selectedMerchantId]);

  if (mappedMerchants.length === 0) {
    return <div className="merchant-map-shell"><div className="map-fallback">위치가 등록된 사용처가 없습니다.</div></div>;
  }

  return <div className="merchant-map-shell">
    <div ref={containerRef} className="merchant-map" aria-label="RunMile 사용처 지도" />
    {error && <div className="map-fallback">{error}</div>}
    {!error && <span className="map-count">지도에 {mappedMerchants.length.toLocaleString("ko-KR")}곳 표시</span>}
  </div>;
}
