"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import KakaoMerchantMap from "@/components/KakaoMerchantMap";
import { getApi, postApi, RunMileApiError } from "@/lib/api";
import { formatWon, merchantCategoryLabel, merchantSuggestedAmount } from "@/lib/presentation";
import type { Completion, Merchant, MerchantCategory, NftRecord, Payment, Runner, RunMileTransaction, Wallet } from "@/types/contracts";

const RUNNER_ID = 1;
const INITIAL_MERCHANT_COUNT = 12;
const categories: Array<MerchantCategory | "ALL"> = ["ALL", "RESTAURANT", "CAFE", "RETAIL"];
const mile = (amount: number) => `${amount.toLocaleString("ko-KR")} RunMile`;
const courseName: Record<string, string> = { FULL: "풀코스", TEN_K: "10km", FIVE_K: "5km" };
const courseDistance: Record<string, string> = { FULL: "42.195", TEN_K: "10", FIVE_K: "5" };

export default function ParticipantPage() {
  const [runner, setRunner] = useState<Runner | null>(null);
  const [completion, setCompletion] = useState<Completion | null>(null);
  const [nft, setNft] = useState<NftRecord | null>(null);
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<RunMileTransaction[]>([]);
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [selectedMerchant, setSelectedMerchant] = useState<Merchant | null>(null);
  const [category, setCategory] = useState<MerchantCategory | "ALL">("ALL");
  const [district, setDistrict] = useState("ALL");
  const [visibleCount, setVisibleCount] = useState(INITIAL_MERCHANT_COUNT);
  const [merchantView, setMerchantView] = useState<"LIST" | "MAP">("LIST");
  const [loading, setLoading] = useState(true);
  const [issuing, setIssuing] = useState(false);
  const [paying, setPaying] = useState(false);
  const [notice, setNotice] = useState<string | null>(null);
  const [payment, setPayment] = useState<Payment | null>(null);

  const refreshWallet = async () => {
    const [nextWallet, nextTransactions] = await Promise.all([
      getApi<Wallet>(`/runners/${RUNNER_ID}/wallet`),
      getApi<RunMileTransaction[]>(`/runners/${RUNNER_ID}/runmile/transactions`)
    ]);
    setWallet(nextWallet);
    setTransactions(nextTransactions);
  };

  useEffect(() => {
    async function load() {
      try {
        const [nextRunner, nextCompletion, nextNft, nextMerchants] = await Promise.all([
          getApi<Runner>(`/runners/${RUNNER_ID}`), getApi<Completion>(`/runners/${RUNNER_ID}/completion`),
          getApi<NftRecord>(`/runners/${RUNNER_ID}/nft`), getApi<Merchant[]>("/merchants?runmileEnabled=true")
        ]);
        setRunner(nextRunner); setCompletion(nextCompletion); setNft(nextNft); setMerchants(nextMerchants); setSelectedMerchant(nextMerchants[0] ?? null);
        await refreshWallet();
      } catch (error) { setNotice(error instanceof Error ? error.message : "서비스 정보를 불러오지 못했습니다."); }
      finally { setLoading(false); }
    }
    void load();
  }, []);

  const districts = useMemo(
    () => [...new Set(merchants.map((merchant) => merchant.district))].sort((left, right) => left.localeCompare(right, "ko")),
    [merchants]
  );
  const filteredMerchants = useMemo(
    () => merchants.filter((merchant) =>
      (category === "ALL" || merchant.category === category) &&
      (district === "ALL" || merchant.district === district)
    ),
    [category, district, merchants]
  );
  const visibleMerchants = filteredMerchants.slice(0, visibleCount);
  const totalAmount = selectedMerchant ? merchantSuggestedAmount(selectedMerchant) : 0;
  const runmileAmount = Math.min(wallet?.balance ?? 0, 10000, totalAmount);
  const completionVerified = Boolean(completion?.completed && nft?.verified);
  const rewardReceived = (wallet?.totalIssued ?? 0) > 0;
  const paymentCompleted = Boolean(payment || transactions.some((transaction) => transaction.type === "USE"));

  const changeFilters = (nextCategory: MerchantCategory | "ALL", nextDistrict: string) => {
    const nextMerchants = merchants.filter((merchant) =>
      (nextCategory === "ALL" || merchant.category === nextCategory) &&
      (nextDistrict === "ALL" || merchant.district === nextDistrict)
    );
    setCategory(nextCategory);
    setDistrict(nextDistrict);
    setVisibleCount(INITIAL_MERCHANT_COUNT);
    setSelectedMerchant(nextMerchants[0] ?? null);
    setPayment(null);
  };

  const selectMerchant = useCallback((merchant: Merchant) => {
    setSelectedMerchant(merchant);
    setPayment(null);
  }, []);

  const issue = async () => {
    setIssuing(true); setNotice(null);
    try {
      await postApi(`/runners/${RUNNER_ID}/runmile/issue`, { amount: 10000 });
      await refreshWallet(); setNotice("완주 보상 10,000 RunMile이 지급되었습니다.");
    } catch (error) {
      setNotice(error instanceof RunMileApiError && error.code === "RUNMILE_ALREADY_ISSUED" ? "완주 보상은 이미 지급되어 있습니다." : error instanceof Error ? error.message : "지급 처리에 실패했습니다.");
    } finally { setIssuing(false); }
  };

  const pay = async () => {
    if (!selectedMerchant) return;
    setPaying(true); setNotice(null);
    try {
      const result = await postApi<Payment>("/payments", { runnerId: RUNNER_ID, merchantId: selectedMerchant.id, totalAmount, runmileAmount });
      setPayment(result); await refreshWallet();
    } catch (error) { setNotice(error instanceof Error ? error.message : "결제 처리에 실패했습니다."); }
    finally { setPaying(false); }
  };

  if (loading) return <main className="service-page"><p className="loading-screen">정보를 불러오는 중입니다.</p></main>;

  return (
    <main className="service-page">
      <header className="service-header"><Link className="brand" href="/">RUN<span>MILE</span></Link><div className="user-identity"><span aria-hidden="true">R</span><b>{runner?.runnerCode ?? "RUNNER_00001"}</b></div></header>
      {notice && <div className="service-notice" role="status"><span>{notice}</span><button onClick={() => setNotice(null)} aria-label="알림 닫기">닫기</button></div>}

      <section className="service-hero">
        <div className="achievement-copy">
          <h1>완주를 축하해요,<br />러너님</h1>
          <p>이번 완주 보상</p>
          <strong>+10,000 <span>RunMile</span></strong>
          {!rewardReceived && <button className="primary-action" onClick={issue} disabled={issuing || !completionVerified}>{issuing ? "지급 중" : "보상 받기"}</button>}
          <a className="find-merchant-action" href="#merchants">사용처 찾기</a>
        </div>
        <article className="completion-card">
          <header><div><p>2026 대구마라톤</p><span>{courseName[completion?.course ?? "FULL"]}</span></div><b>{completionVerified ? "완주 인증 완료" : "완주 인증 대기"}</b></header>
          <div className="distance-result"><strong>{courseDistance[completion?.course ?? "FULL"]}</strong><span>km</span></div>
          <div className="run-track" aria-label="출발부터 완주까지의 러닝 경로">
            <div className="track-rail"><span className="moving-point" /><i className="finish-marker">✓</i></div>
            <div className="track-labels"><span>출발</span><span>완주</span></div>
          </div>
          <details><summary>인증 정보 보기</summary><div><p>인증 번호 <code>{nft?.tokenId}</code></p><p>검증 방식 <code>{nft?.network}</code></p></div></details>
        </article>
      </section>

      <ol className="service-flow" aria-label="RunMile 이용 흐름">
        <li className={completionVerified ? "done" : "active"}><span /><b>완주 확인</b></li>
        <li className={rewardReceived ? "done" : completionVerified ? "active" : ""}><span /><b>보상 지급</b></li>
        <li className={paymentCompleted ? "done" : rewardReceived ? "active" : ""}><span /><b>사용처 찾기</b></li>
        <li className={paymentCompleted ? "active" : ""}><span /><b>지역 소비</b></li>
      </ol>

      <section className="account-overview" aria-label="RunMile 지갑">
        <div className="balance-overview"><p>사용 가능한 RunMile</p><strong>{(wallet?.balance ?? 0).toLocaleString("ko-KR")}</strong><span>총 지급 {(wallet?.totalIssued ?? 0).toLocaleString("ko-KR")} · 사용 {(wallet?.totalUsed ?? 0).toLocaleString("ko-KR")}</span></div>
      </section>

      <section className="merchant-finder" id="merchants">
        <div className="section-intro"><div><h2>어디에서 쓸까요?</h2></div><button className="subtle-action" type="button" aria-pressed={merchantView === "MAP"} onClick={() => setMerchantView((view) => view === "LIST" ? "MAP" : "LIST")}>{merchantView === "LIST" ? "지도 보기" : "목록 보기"}</button></div>
        <div className="merchant-filters">
          <div className="filter-control" role="tablist" aria-label="가맹점 업종 필터">{categories.map((value) => <button key={value} type="button" role="tab" aria-selected={category === value} className={category === value ? "selected" : ""} onClick={() => changeFilters(value, district)}>{value === "ALL" ? "전체" : merchantCategoryLabel[value]}</button>)}</div>
          <label className="district-filter">지역<select value={district} onChange={(event) => changeFilters(category, event.target.value)}><option value="ALL">전체 구·군</option>{districts.map((value) => <option key={value} value={value}>{value}</option>)}</select></label>
        </div>
        <p className="merchant-result-count">사용처 {filteredMerchants.length.toLocaleString("ko-KR")}곳</p>
        {merchantView === "MAP" ? <KakaoMerchantMap merchants={filteredMerchants} selectedMerchantId={selectedMerchant?.id ?? null} onSelect={selectMerchant} /> : <><div className="merchant-results">{visibleMerchants.map((merchant) => <button key={merchant.id} onClick={() => selectMerchant(merchant)} className={`merchant-result ${selectedMerchant?.id === merchant.id ? "selected" : ""}`}>
          <span className="merchant-monogram">{merchant.name.slice(0, 1)}</span><span className="merchant-copy"><b>{merchant.name}</b><span>{merchantCategoryLabel[merchant.category]} · {merchant.district}</span><small>{merchant.address} · 예시 결제 {formatWon(merchantSuggestedAmount(merchant))} · RunMile 사용 가능</small></span><span className="merchant-select-text">선택</span>
        </button>)}{filteredMerchants.length === 0 && <p className="merchant-empty">조건에 맞는 사용처가 없습니다.</p>}</div>
        {visibleCount < filteredMerchants.length && <button className="merchant-more" type="button" onClick={() => setVisibleCount((count) => count + INITIAL_MERCHANT_COUNT)}>사용처 더보기 <span>{Math.min(INITIAL_MERCHANT_COUNT, filteredMerchants.length - visibleCount)}곳</span></button>}</>}
      </section>

      <section className="payment-area" aria-labelledby="payment-title"><div className="payment-header"><div><h2 id="payment-title">결제 미리보기</h2><p>{selectedMerchant?.name ?? "가맹점을 선택해 주세요"}</p></div>{selectedMerchant && <span>RunMile 사용 가능</span>}</div>
        {payment ? <div className="payment-complete"><p>결제가 완료되었습니다</p><strong>{formatWon(payment.totalAmount)}</strong><dl><div><dt>RunMile 사용</dt><dd>- {mile(payment.runmileAmount)}</dd></div><div><dt>개인결제</dt><dd>{formatWon(payment.personalAmount)}</dd></div></dl><small>결제번호 {payment.paymentId}</small></div> : <div className="payment-content"><dl><div><dt>총 결제금액</dt><dd>{formatWon(totalAmount)}</dd></div><div><dt>RunMile 사용</dt><dd className="brand-value">- {mile(runmileAmount)}</dd></div><div><dt>개인결제</dt><dd>{formatWon(totalAmount - runmileAmount)}</dd></div></dl><button className="primary-action payment-action" onClick={pay} disabled={paying || !selectedMerchant || runmileAmount === 0}>{paying ? "결제 승인 중" : `${formatWon(totalAmount)} 결제하기`}</button></div>}
      </section>

      <section className="usage-history"><div className="section-intro"><div><h2>RunMile 내역</h2></div><span>{transactions.length}건</span></div><div>{transactions.map((item) => <article className="usage-row" key={item.id}><div><b>{item.type === "ISSUE" ? "완주 보상" : "RunMile 사용"}</b><span>{new Date(item.createdAt).toLocaleString("ko-KR")}</span></div><strong className={item.type === "ISSUE" ? "brand-value" : ""}>{item.type === "ISSUE" ? "+" : "−"}{mile(item.amount)}</strong></article>)}</div></section>
    </main>
  );
}
