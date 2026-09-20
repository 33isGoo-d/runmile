"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { formatWon, merchantCategoryLabel, merchantSuggestedAmount } from "@/lib/presentation";
import type { Merchant } from "@/types/contracts";

interface KakaoLatLng {}
interface KakaoMarker {
  setMap(map: KakaoMapInstance | null): void;
  setOpacity(opacity: number): void;
  setZIndex(zIndex: number): void;
}
interface KakaoLatLngBounds {
  extend(position: KakaoLatLng): void;
}
interface KakaoMapInstance {
  getLevel(): number;
  panTo(position: KakaoLatLng): void;
  setBounds(bounds: KakaoLatLngBounds): void;
  setLevel(level: number, options?: { anchor?: KakaoLatLng }): void;
}
interface KakaoInfoWindow {
  close(): void;
  open(map: KakaoMapInstance, marker: KakaoMarker): void;
}
interface KakaoMarkerClusterer {
  addMarkers(markers: KakaoMarker[]): void;
  clear(): void;
}
interface KakaoMaps {
  load(callback: () => void): void;
  LatLng: new (latitude: number, longitude: number) => KakaoLatLng;
  LatLngBounds: new () => KakaoLatLngBounds;
  Map: new (container: HTMLElement, options: { center: KakaoLatLng; level: number }) => KakaoMapInstance;
  Marker: new (options: { map?: KakaoMapInstance; position: KakaoLatLng; title: string }) => KakaoMarker;
  InfoWindow: new (options: { content: HTMLElement; removable: boolean }) => KakaoInfoWindow;
  MarkerClusterer?: new (options: {
    map: KakaoMapInstance;
    averageCenter: boolean;
    minLevel: number;
    styles?: Array<Record<string, string>>;
  }) => KakaoMarkerClusterer;
  event: {
    addListener(target: KakaoMarker, event: "click", handler: () => void): void;
    removeListener(target: KakaoMarker, event: "click", handler: () => void): void;
  };
}

declare global {
  interface Window {
    kakao?: { maps: KakaoMaps };
  }
}

type MarkerEntry = {
  marker: KakaoMarker;
  position: KakaoLatLng;
  clickHandler: () => void;
};

const DAEGU_CENTER = { latitude: 35.8714, longitude: 128.6014 };
let sdkPromise: Promise<KakaoMaps> | null = null;

function loadSdk(apiKey: string): Promise<KakaoMaps> {
  if (window.kakao?.maps) {
    return new Promise((resolve) => window.kakao?.maps.load(() => resolve(window.kakao!.maps)));
  }
  if (sdkPromise) return sdkPromise;

  sdkPromise = new Promise((resolve, reject) => {
    const script = document.createElement("script");
    script.src = `https://dapi.kakao.com/v2/maps/sdk.js?appkey=${encodeURIComponent(apiKey)}&autoload=false&libraries=clusterer`;
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

function createInfoContent(merchant: Merchant): HTMLElement {
  const content = document.createElement("div");
  content.className = "map-info-window";

  const name = document.createElement("strong");
  name.textContent = merchant.name;
  const meta = document.createElement("span");
  meta.textContent = `${merchantCategoryLabel[merchant.category]} · ${merchant.district}`;
  const price = document.createElement("b");
  price.textContent = `예시 결제 ${formatWon(merchantSuggestedAmount(merchant))}`;
  const address = document.createElement("small");
  address.textContent = merchant.address;

  content.append(name, meta, price, address);
  return content;
}

type Props = {
  merchants: Merchant[];
  selectedMerchantId: number | null;
  onSelect: (merchant: Merchant) => void;
};

export default function KakaoMerchantMap({ merchants, selectedMerchantId, onSelect }: Props) {
  const containerRef = useRef<HTMLDivElement>(null);
  const mapsRef = useRef<KakaoMaps | null>(null);
  const mapRef = useRef<KakaoMapInstance | null>(null);
  const clustererRef = useRef<KakaoMarkerClusterer | null>(null);
  const infoWindowRef = useRef<KakaoInfoWindow | null>(null);
  const markersRef = useRef<Map<number, MarkerEntry>>(new Map());
  const onSelectRef = useRef(onSelect);
  const initialMerchantsRef = useRef(merchants);
  const [ready, setReady] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const apiKey = process.env.NEXT_PUBLIC_KAKAO_MAP_API_KEY;
  const mappedMerchants = useMemo(
    () => merchants.filter((merchant) => merchant.latitude != null && merchant.longitude != null),
    [merchants]
  );
  const selectedMerchant = useMemo(
    () => mappedMerchants.find((merchant) => merchant.id === selectedMerchantId) ?? null,
    [mappedMerchants, selectedMerchantId]
  );

  useEffect(() => {
    onSelectRef.current = onSelect;
  }, [onSelect]);

  useEffect(() => {
    let active = true;
    if (!apiKey || apiKey.startsWith("replace-with")) {
      setError("지도 API 키를 설정하면 사용처 위치를 확인할 수 있습니다.");
      return;
    }
    if (!containerRef.current) return;

    setError(null);
    void loadSdk(apiKey).then((maps) => {
      if (!active || !containerRef.current) return;
      const initialMerchant = initialMerchantsRef.current.find(
        (merchant) => merchant.latitude != null && merchant.longitude != null
      );
      const center = initialMerchant
        ? new maps.LatLng(initialMerchant.latitude!, initialMerchant.longitude!)
        : new maps.LatLng(DAEGU_CENTER.latitude, DAEGU_CENTER.longitude);

      mapsRef.current = maps;
      mapRef.current = new maps.Map(containerRef.current, { center, level: 7 });
      if (maps.MarkerClusterer) {
        clustererRef.current = new maps.MarkerClusterer({
          map: mapRef.current,
          averageCenter: true,
          minLevel: 6,
          styles: [{
            width: "42px",
            height: "42px",
            border: "3px solid rgba(255,255,255,.92)",
            borderRadius: "50%",
            background: "rgba(196,59,50,.9)",
            boxShadow: "0 3px 10px rgba(11,28,48,.24)",
            color: "#fff",
            fontSize: "12px",
            fontWeight: "800",
            lineHeight: "36px",
            textAlign: "center"
          }]
        });
      }
      setReady(true);
    }).catch((sdkError: unknown) => {
      if (active) setError(sdkError instanceof Error ? sdkError.message : "지도를 표시하지 못했습니다.");
    });

    return () => {
      active = false;
      infoWindowRef.current?.close();
      clustererRef.current?.clear();
      markersRef.current.forEach(({ marker, clickHandler }) => {
        mapsRef.current?.event.removeListener(marker, "click", clickHandler);
        marker.setMap(null);
      });
      markersRef.current.clear();
      mapRef.current = null;
      mapsRef.current = null;
      clustererRef.current = null;
      infoWindowRef.current = null;
    };
  }, [apiKey]);

  useEffect(() => {
    const maps = mapsRef.current;
    const map = mapRef.current;
    if (!ready || !maps || !map) return;

    infoWindowRef.current?.close();
    infoWindowRef.current = null;
    clustererRef.current?.clear();
    markersRef.current.forEach(({ marker, clickHandler }) => {
      maps.event.removeListener(marker, "click", clickHandler);
      marker.setMap(null);
    });
    markersRef.current.clear();

    if (mappedMerchants.length === 0) return;

    const bounds = new maps.LatLngBounds();
    const nextMarkers: KakaoMarker[] = [];
    mappedMerchants.forEach((merchant) => {
      const position = new maps.LatLng(merchant.latitude!, merchant.longitude!);
      const marker = new maps.Marker({
        map: clustererRef.current ? undefined : map,
        position,
        title: merchant.name
      });
      const clickHandler = () => {
        infoWindowRef.current?.close();
        const infoWindow = new maps.InfoWindow({
          content: createInfoContent(merchant),
          removable: false
        });
        infoWindow.open(map, marker);
        infoWindowRef.current = infoWindow;
        marker.setOpacity(1);
        marker.setZIndex(10);
        map.panTo(position);
        onSelectRef.current(merchant);
      };
      maps.event.addListener(marker, "click", clickHandler);
      marker.setOpacity(0.82);
      markersRef.current.set(merchant.id, { marker, position, clickHandler });
      nextMarkers.push(marker);
      bounds.extend(position);
    });

    clustererRef.current?.addMarkers(nextMarkers);
    map.setBounds(bounds);
  }, [mappedMerchants, ready]);

  useEffect(() => {
    const maps = mapsRef.current;
    const map = mapRef.current;
    if (!ready || !maps || !map) return;

    markersRef.current.forEach(({ marker }) => {
      marker.setOpacity(0.82);
      marker.setZIndex(0);
    });
    infoWindowRef.current?.close();
    infoWindowRef.current = null;

    if (selectedMerchantId == null) return;
    const selectedEntry = markersRef.current.get(selectedMerchantId);
    if (!selectedMerchant || !selectedEntry) return;

    if (map.getLevel() > 4) map.setLevel(4, { anchor: selectedEntry.position });
    selectedEntry.marker.setOpacity(1);
    selectedEntry.marker.setZIndex(10);
    const infoWindow = new maps.InfoWindow({
      content: createInfoContent(selectedMerchant),
      removable: false
    });
    infoWindow.open(map, selectedEntry.marker);
    infoWindowRef.current = infoWindow;
    map.panTo(selectedEntry.position);
  }, [mappedMerchants, ready, selectedMerchantId]);

  const noLocations = mappedMerchants.length === 0;

  return <div className="merchant-map-shell">
    <div ref={containerRef} className="merchant-map" aria-label="RunMile 사용처 지도" />
    {error && <div className="map-fallback">{error}</div>}
    {!error && noLocations && <div className="map-fallback">위치가 등록된 사용처가 없습니다.</div>}
    {!error && !noLocations && <div className="map-legend"><span><i />개별 가맹점</span><span><i />주변 가맹점 묶음</span></div>}
    {!error && selectedMerchant && <div className="map-selection"><span>결제 대상</span><b>{selectedMerchant.name}</b><strong>{formatWon(merchantSuggestedAmount(selectedMerchant))}</strong></div>}
    {!error && !noLocations && <span className="map-count">{mappedMerchants.length.toLocaleString("ko-KR")}곳 표시</span>}
  </div>;
}
