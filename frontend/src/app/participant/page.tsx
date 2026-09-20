"use client";

import Link from "next/link";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { ReactNode } from "react";
import KakaoMerchantMap from "@/components/KakaoMerchantMap";
import { getApi, postApi, RunMileApiError } from "@/lib/api";
import { formatWon, merchantCategoryLabel, merchantSuggestedAmount } from "@/lib/presentation";
import type { Completion, Merchant, MerchantCategory, NftRecord, Payment, Runner, RunMileIssueResponse, RunMileTransaction, Wallet } from "@/types/contracts";

const RUNNER_ID = 1;
type Stage = "VERIFY" | "REWARD" | "MERCHANT" | "PAYMENT";
type VerificationStatus = "IDLE" | "CHECKING" | "VERIFIED" | "PENDING" | "ERROR";
type MerchantFilter = "ALL" | MerchantCategory;
const steps: Array<{ key: Stage; label: string; note: string }> = [
  { key: "VERIFY", label: "완주 인증", note: "완주증명" },
  { key: "REWARD", label: "RunMile 지급", note: "완주 보상" },
  { key: "MERCHANT", label: "사용처 선택", note: "가맹점" },
  { key: "PAYMENT", label: "결제 확인", note: "결제 금액" }
];

export default function ParticipantPage() {
  const [stage, setStage] = useState<Stage>("VERIFY");
  const [verification, setVerification] = useState<VerificationStatus>("IDLE");
  const [runner, setRunner] = useState<Runner | null>(null);
  const [completion, setCompletion] = useState<Completion | null>(null);
  const [nft, setNft] = useState<NftRecord | null>(null);
  const [wallet, setWallet] = useState<Wallet | null>(null);
  const [transactions, setTransactions] = useState<RunMileTransaction[]>([]);
  const [merchants, setMerchants] = useState<Merchant[]>([]);
  const [selectedMerchant, setSelectedMerchant] = useState<Merchant | null>(null);
  const [merchantView, setMerchantView] = useState<"LIST" | "MAP">("LIST");
  const [payment, setPayment] = useState<Payment | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [issuing, setIssuing] = useState(false);
  const [paying, setPaying] = useState(false);
  const [loading, setLoading] = useState(true);
  const [merchantFilter, setMerchantFilter] = useState<MerchantFilter>("ALL");

  const loadParticipantState = useCallback(async () => {
    setLoading(true);
    setMessage(null);
    try {
      const [nextRunner, nextCompletion, nextNft, nextWallet, nextTransactions, nextMerchants, nextPayments] = await Promise.all([
        getApi<Runner>(`/runners/${RUNNER_ID}`),
        getApi<Completion>(`/runners/${RUNNER_ID}/completion`),
        getApi<NftRecord>(`/runners/${RUNNER_ID}/nft`),
        getApi<Wallet>(`/runners/${RUNNER_ID}/wallet`),
        getApi<RunMileTransaction[]>(`/runners/${RUNNER_ID}/runmile/transactions`),
        getApi<Merchant[]>("/merchants?runmileEnabled=true"),
        getApi<Payment[]>(`/runners/${RUNNER_ID}/payments`)
      ]);
      const latestPayment = nextPayments.find((item) => item.status === "SUCCESS") ?? null;
      const restoredMerchant = latestPayment
        ? nextMerchants.find((merchant) => merchant.id === latestPayment.merchantId) ?? null
        : null;

      setRunner(nextRunner);
      setCompletion(nextCompletion);
      setNft(nextNft);
      setWallet(nextWallet);
      setTransactions(nextTransactions);
      setMerchants(nextMerchants);
      setSelectedMerchant(restoredMerchant);
      setPayment(latestPayment);
      setVerification(nextCompletion.completed && nextNft.verified ? "VERIFIED" : "PENDING");
    } catch (error) {
      setVerification("ERROR");
      setMessage(error instanceof Error ? error.message : "참가자 이용 상태를 불러오지 못했습니다.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void loadParticipantState(); }, [loadParticipantState]);
  const rewardReceived = transactions.some((transaction) => transaction.type === "ISSUE");
  const totalAmount = selectedMerchant ? merchantSuggestedAmount(selectedMerchant) : 0;
  const runmileAmount = Math.min(wallet?.balance ?? 0, 10_000, totalAmount);
  const displayedTotalAmount = payment?.totalAmount ?? totalAmount;
  const displayedRunmileAmount = payment?.runmileAmount ?? runmileAmount;
  const displayedPersonalAmount = payment?.personalAmount ?? totalAmount - runmileAmount;
  const availableSteps = useMemo(() => {
    if (verification !== "VERIFIED") return ["VERIFY"];
    if (!rewardReceived) return ["VERIFY", "REWARD"];
    return selectedMerchant || payment
      ? ["VERIFY", "REWARD", "MERCHANT", "PAYMENT"]
      : ["VERIFY", "REWARD", "MERCHANT"];
  }, [verification, rewardReceived, selectedMerchant, payment]);
  const visibleMerchants = useMemo(
    () => merchantFilter === "ALL" ? merchants : merchants.filter((merchant) => merchant.category === merchantFilter),
    [merchantFilter, merchants]
  );

  const selectMerchant = (merchant: Merchant) => {
    setSelectedMerchant(merchant);
    setPayment(null);
    setMessage(null);
  };

  const changeMerchantFilter = (category: MerchantFilter) => {
    setMerchantFilter(category);
    if (category !== "ALL" && selectedMerchant?.category !== category) {
      setSelectedMerchant(null);
      setPayment(null);
    }
  };

  const refreshWallet = async () => {
    const [nextWallet, nextTransactions] = await Promise.all([getApi<Wallet>(`/runners/${RUNNER_ID}/wallet`), getApi<RunMileTransaction[]>(`/runners/${RUNNER_ID}/runmile/transactions`)]);
    setWallet(nextWallet); setTransactions(nextTransactions);
  };
  const issueReward = async () => {
    setIssuing(true); setMessage(null);
    try {
      const result = await postApi<RunMileIssueResponse>(`/runners/${RUNNER_ID}/runmile/issue`, { amount: 10_000 });
      setWallet((current) => ({ balance: result.balance, totalIssued: (current?.totalIssued ?? 0) + result.issuedAmount, totalUsed: current?.totalUsed ?? 0 }));
      const nextTransactions = await getApi<RunMileTransaction[]>(`/runners/${RUNNER_ID}/runmile/transactions`);
      setTransactions(nextTransactions);
    }
    catch (error) {
      if (error instanceof RunMileApiError && error.code === "RUNMILE_ALREADY_ISSUED") { await refreshWallet(); return; }
      setMessage(error instanceof Error ? error.message : "RunMile 지급 실패");
    } finally { setIssuing(false); }
  };
  const verifyCompletion = async () => {
    setVerification("CHECKING"); setMessage(null);
    try {
      const [nextCompletion, nextNft] = await Promise.all([getApi<Completion>(`/runners/${RUNNER_ID}/completion`), getApi<NftRecord>(`/runners/${RUNNER_ID}/nft`)]);
      setCompletion(nextCompletion); setNft(nextNft); await refreshWallet();
      const verified = nextCompletion.completed && nextNft.verified;
      setVerification(verified ? "VERIFIED" : "PENDING");
    } catch (error) { setVerification("ERROR"); setMessage(error instanceof Error ? error.message : "완주증명 확인 실패"); }
  };
  const createPayment = async () => {
    if (!selectedMerchant) return;
    setPaying(true); setMessage(null);
    try { const result = await postApi<Payment>("/payments", { runnerId: RUNNER_ID, merchantId: selectedMerchant.id, totalAmount, runmileAmount }); setPayment(result); await refreshWallet(); }
    catch (error) { setMessage(error instanceof Error ? error.message : "결제 처리 실패"); } finally { setPaying(false); }
  };

  return <main className="participant-app-shell">
    <header className="participant-app-header app-common-header">
      <Link className="brand" href="/">RUN<span>MILE</span></Link><span className="participant-event">2026 대구마라톤</span><span className="participant-runner">{runner?.runnerCode ?? "RUNNER_00001"}</span>
    </header>
    <section className="participant-app-content">
      <nav className="participant-stepper" aria-label="RunMile 이용 단계">{steps.map((item, index) => <button key={item.key} type="button" disabled={loading || !availableSteps.includes(item.key)} aria-current={stage === item.key ? "step" : undefined} className={stage === item.key ? "active" : ""} onClick={() => setStage(item.key)}><span>{String(index + 1).padStart(2, "0")}</span><b>{item.label}</b><small>{item.note}</small></button>)}</nav>
      {message && <p className="participant-app-message" role="alert">{message}</p>}

      <section className={`participant-stage-card stage-${stage.toLowerCase()}`}>
        {loading && <div className="participant-loading" aria-live="polite"><span /><b>참가자 이용 상태를 불러오는 중</b><p>완주증명, RunMile, 결제 기록을 확인하고 있습니다.</p></div>}
        {!loading && message && verification === "ERROR" && <div className="participant-state"><b>이용 상태를 불러오지 못했습니다.</b><p>서버 연결을 확인한 뒤 다시 시도해 주세요.</p><button className="participant-secondary" type="button" onClick={() => void loadParticipantState()}>다시 불러오기</button></div>}
        {!loading && !(message && verification === "ERROR") && <>
        {stage === "VERIFY" && <><StageHeading step="01" label="완주 인증" title={<>대구마라톤<br />완주 인증</>} copy="완주 기록과 완주증명 확인" />
          {verification === "IDLE" && <><VerificationCycle /><button className="participant-primary" type="button" onClick={verifyCompletion}>완주증명 확인</button></>}
          {verification === "CHECKING" && <><VerificationCycle checking /><p className="participant-status">완주증명 확인 진행</p></>}
          {verification === "PENDING" && <div className="participant-state"><b>완주증명 등록 대기</b><p>완주증명 등록 후 재확인</p><button className="participant-secondary" type="button" onClick={verifyCompletion}>재확인</button></div>}
          {verification === "ERROR" && <div className="participant-state"><b>완주증명 확인 실패</b><button className="participant-secondary" type="button" onClick={verifyCompletion}>재시도</button></div>}
          {verification === "VERIFIED" && <div className="participant-proof-pass"><header><span>완주 인증 완료</span><b>체인 검증 완료</b></header><div className="participant-finisher"><small>2026 DAEGU MARATHON</small><div><strong>FINISHER</strong><b>{courseDistance(completion?.course)} km</b></div><p>{courseLabel(completion?.course)} · {completion ? formatFinishTime(completion.finishTimeSeconds) : "기록 확인 중"}{completion ? ` · ${formatCompletionDate(completion.completedAt)}` : ""}</p></div><dl><div><dt>참가자</dt><dd>{runner?.runnerCode}</dd></div><div><dt>검증 네트워크</dt><dd>{nft?.network}</dd></div><div><dt>거래 증명</dt><dd title={nft?.tokenId}>{nft?.tokenId}</dd></div></dl>{nft?.tokenId && <a className="participant-proof-link" href={polygonExplorerUrl(nft.network, nft.tokenId)} target="_blank" rel="noreferrer">블록체인 거래 확인</a>}<button className="participant-primary" type="button" onClick={() => setStage("REWARD")}>완주 보상 받기 <span>→</span></button></div>}
        </>}

        {stage === "REWARD" && <><StageHeading step="02" label="RunMile 지급" title={<>완주 보상<br />RunMile</>} copy="지급 가능 완주 보상" />
          <div className="participant-reward-card"><span>2026 대구마라톤 {courseLabel(completion?.course)} 보상</span><strong>10,000 <small>RunMile</small></strong><p>완주의 성취를 대구 지역 소비로 연결</p></div>
          {rewardReceived ? <div className="participant-wallet-card"><b>완주 보상 지급 완료</b><dl><div><dt>누적 지급</dt><dd>{(wallet?.totalIssued ?? 0).toLocaleString("ko-KR")} RunMile</dd></div><div><dt>사용</dt><dd>{(wallet?.totalUsed ?? 0).toLocaleString("ko-KR")} RunMile</dd></div><div className="wallet-balance"><dt>현재 잔액</dt><dd>{(wallet?.balance ?? 0).toLocaleString("ko-KR")} RunMile</dd></div></dl><button className="participant-dark-action" type="button" onClick={() => setStage(payment ? "PAYMENT" : "MERCHANT")}>{payment ? "결제 내역 확인" : "사용처 선택"} <span>→</span></button></div> : <button className="participant-primary" type="button" disabled={issuing} onClick={issueReward}>{issuing ? "지급 처리" : "RunMile 지급"}</button>}
        </>}

        {stage === "MERCHANT" && <><StageHeading step="03" label="사용처 선택" title={<>RunMile 사용처</>} copy="지역 가맹점 선택" />
          <div className="participant-view-tabs"><button className={merchantView === "LIST" ? "active" : ""} type="button" onClick={() => setMerchantView("LIST")}>목록</button><button className={merchantView === "MAP" ? "active" : ""} type="button" onClick={() => setMerchantView("MAP")}>지도</button></div>
          <div className="participant-merchant-filters" aria-label="가맹점 카테고리">{(["ALL", "RESTAURANT", "CAFE", "RETAIL"] as MerchantFilter[]).map((category) => <button key={category} type="button" className={merchantFilter === category ? "active" : ""} onClick={() => changeMerchantFilter(category)}>{category === "ALL" ? "전체" : merchantCategoryLabel[category]}</button>)}</div>
          <p className="participant-merchant-count">사용 가능한 가맹점 {visibleMerchants.length}곳</p>
          {merchantView === "MAP" ? <KakaoMerchantMap merchants={visibleMerchants} selectedMerchantId={selectedMerchant?.id ?? null} onSelect={selectMerchant} /> : <div className="participant-merchant-list">{visibleMerchants.map((merchant, index) => <button type="button" key={merchant.id} className={selectedMerchant?.id === merchant.id ? "selected" : ""} onClick={() => selectMerchant(merchant)}><span className="merchant-icon">{["☕", "◒", "◇"][index % 3]}</span><span><small>{merchantCategoryLabel[merchant.category]}</small><b>{merchant.name}</b><em>{merchant.district} · {merchant.address}</em></span><strong>{selectedMerchant?.id === merchant.id ? "✓" : "+"}</strong></button>)}</div>}
          {selectedMerchant && <div className="participant-selected-merchant"><span>선택한 사용처</span><b>{selectedMerchant.name}</b><strong>{formatWon(totalAmount)}</strong></div>}
          <button className="participant-primary" type="button" disabled={!selectedMerchant} onClick={() => setStage("PAYMENT")}>결제 확인 <span>→</span></button>
        </>}

        {stage === "PAYMENT" && <><StageHeading step="04" label="결제 확인" title={<>결제 금액<br />확인</>} copy="RunMile 사용과 개인 결제" />
          <div className="participant-payment-card"><header><span>가맹점</span><b>{payment?.merchantName ?? selectedMerchant?.name}</b></header><dl><div><dt>총 결제 금액</dt><dd>{formatWon(displayedTotalAmount)}</dd></div><div className="runmile-row"><dt>RunMile 사용</dt><dd>- {displayedRunmileAmount.toLocaleString("ko-KR")} RunMile</dd></div><div className="personal-row"><dt>개인 결제</dt><dd>{formatWon(displayedPersonalAmount)}</dd></div></dl></div>
          {payment ? <><div className="participant-receipt"><b>결제 완료</b><span>{payment.merchantName} · {formatWon(payment.totalAmount)}</span><span>RunMile {payment.runmileAmount.toLocaleString("ko-KR")} 사용 · 개인 결제 {formatWon(payment.personalAmount)}</span><small>{formatPaidAt(payment.paidAt)} · 결제번호 {payment.paymentId}</small></div><Link className="participant-admin-link" href="/admin">관리자 분석 <span>→</span></Link></> : <button className="participant-primary" type="button" disabled={paying || !selectedMerchant} onClick={createPayment}>{paying ? "결제 처리" : `${formatWon(totalAmount)} 결제`} </button>}
          {transactions.length > 0 && <div className="participant-ledger"><b>최근 RunMile 이용 내역</b>{transactions.slice(0, 3).map((item) => <span key={item.id}>{item.type === "ISSUE" ? "+" : "-"}{item.amount.toLocaleString("ko-KR")} RunMile</span>)}</div>}
        </>}
        </>}
      </section>
    </section>
  </main>;
}

function formatFinishTime(totalSeconds: number) {
  const hours = Math.floor(totalSeconds / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  return [hours, minutes, seconds].map((value) => String(value).padStart(2, "0")).join(":");
}

function courseLabel(course?: Completion["course"]) {
  if (course === "FULL") return "풀코스 완주";
  if (course === "TEN_K") return "10km 완주";
  if (course === "FIVE_K") return "5km 완주";
  return "완주 기록";
}

function courseDistance(course?: Completion["course"]) {
  if (course === "FULL") return "42.195";
  if (course === "TEN_K") return "10";
  if (course === "FIVE_K") return "5";
  return "-";
}

function formatCompletionDate(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { month: "long", day: "numeric" }).format(new Date(value));
}

function formatPaidAt(value: string) {
  return new Intl.DateTimeFormat("ko-KR", { dateStyle: "medium", timeStyle: "short" }).format(new Date(value));
}

function polygonExplorerUrl(network: string, transactionHash: string) {
  const host = network === "POLYGON_AMOY" ? "https://amoy.polygonscan.com" : "https://polygonscan.com";
  return `${host}/tx/${transactionHash}`;
}

function StageHeading({ step, label, title, copy }: { step: string; label: string; title: ReactNode; copy: string }) {
  return <header className="participant-stage-heading"><span>STEP {step} · {label}</span><h1>{title}</h1><p>{copy}</p></header>;
}

function VerificationCycle({ checking = false }: { checking?: boolean }) {
  const labels = ["참가자 완주 기록", "완주증명 정보", "블록체인 거래", "지급 자격"];
  return <div className={checking ? "participant-cycle checking" : "participant-cycle"}><b>완주증명 확인 과정</b>{labels.map((label, index) => <div key={label}><span>{checking ? "✓" : String(index + 1)}</span><p>{label}</p><small>{checking ? "확인 완료" : "대기"}</small></div>)}</div>;
}
