"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getApi } from "@/lib/api";
import { formatWon, merchantCategoryLabel } from "@/lib/presentation";
import type { AiEvaluation, AnalyticsOverview, CategoryAnalytics, DistrictAnalytics, Insight, PolicyEffect, Scenario } from "@/types/contracts";

const compactWon = (value: number) => `${(value / 100000000).toFixed(value >= 100000000 ? 1 : 2)}억`;
const formatEffectRatio = (value: number | null) => value == null ? "산정 불가" : `${value.toFixed(2)}배`;
const scenarios: Array<{ value: Scenario; label: string }> = [
  { value: "NONE", label: "미시행" },
  { value: "LOW", label: "낮음" },
  { value: "MEDIUM", label: "중간" },
  { value: "HIGH", label: "높음" }
];

function Bar({ label, value, max, detail, tone = "mint" }: { label: string; value: number; max: number; detail: string; tone?: "mint" | "blue" }) {
  return <div className="bar-row"><div className="bar-label"><b>{label}</b><span>{detail}</span></div><div className="bar-track"><i className={tone} style={{ width: `${Math.max(8, (value / Math.max(max, 1)) * 100)}%` }} /></div><strong>{formatWon(value)}</strong></div>;
}

export default function AdminPage() {
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [districts, setDistricts] = useState<DistrictAnalytics[]>([]);
  const [categories, setCategories] = useState<CategoryAnalytics[]>([]);
  const [effects, setEffects] = useState<PolicyEffect[]>([]);
  const [insights, setInsights] = useState<Insight[]>([]);
  const [evaluation, setEvaluation] = useState<AiEvaluation>({ baseline: null, effects: [] });
  const [scenario, setScenario] = useState<Scenario>("MEDIUM");
  const [loading, setLoading] = useState(true);
  const [staticError, setStaticError] = useState<string | null>(null);
  const [scenarioError, setScenarioError] = useState<string | null>(null);
  const [staticRetry, setStaticRetry] = useState(0);
  const [scenarioRetry, setScenarioRetry] = useState(0);
  const [reportDate, setReportDate] = useState("");

  useEffect(() => {
    setReportDate(new Intl.DateTimeFormat("ko-KR", {
      year: "numeric", month: "2-digit", day: "2-digit"
    }).format(new Date()));
    let active = true;
    setStaticError(null);
    void Promise.all([
      getApi<CategoryAnalytics[]>("/admin/analytics/categories"),
      getApi<AiEvaluation>("/admin/analytics/evaluation")
    ]).then(([nextCategories, nextEvaluation]) => {
      if (!active) return;
      setCategories(nextCategories);
      setEvaluation(nextEvaluation);
    }).catch((loadError: unknown) => {
      if (active) setStaticError(loadError instanceof Error ? loadError.message : "공통 분석 데이터를 불러오지 못했습니다.");
    });
    return () => { active = false; };
  }, [staticRetry]);

  useEffect(() => {
    let active = true;
    const query = `?scenario=${scenario}`;
    setLoading(true);
    setScenarioError(null);
    void Promise.all([
      getApi<AnalyticsOverview>(`/admin/analytics/overview${query}`), getApi<DistrictAnalytics[]>(`/admin/analytics/districts${query}`),
      getApi<PolicyEffect[]>(`/admin/analytics/effects${query}`), getApi<Insight[]>(`/admin/analytics/insights${query}`)
    ]).then(([nextOverview, nextDistricts, nextEffects, nextInsights]) => {
      if (!active) return;
      setOverview(nextOverview); setDistricts(nextDistricts); setEffects(nextEffects); setInsights(nextInsights);
    }).catch((loadError: unknown) => {
      if (active) setScenarioError(loadError instanceof Error ? loadError.message : "분석 데이터를 불러오지 못했습니다.");
    }).finally(() => {
      if (active) setLoading(false);
    });
    return () => { active = false; };
  }, [scenario, scenarioRetry]);

  const retryFailedRequests = () => {
    if (staticError) setStaticRetry((value) => value + 1);
    if (scenarioError) setScenarioRetry((value) => value + 1);
  };

  const error = scenarioError ?? staticError;
  if (error && !overview) return <main className="admin-shell"><div className="loading-screen loading-error"><span>{error}</span><button type="button" onClick={retryFailedRequests}>다시 시도</button></div></main>;
  if (!overview) return <main className="admin-shell"><p className="loading-screen">정책 분석 데이터 로딩 중</p></main>;
  const maxDistrict = Math.max(...districts.map((item) => item.runmileUsed));
  const maxCategory = Math.max(...categories.map((item) => item.linkedPaymentAmount));
  const topEffect = effects[0];

  return <main className="admin-shell">
    <aside className="admin-sidebar"><Link className="brand" href="/">RUN<span>MILE</span></Link><div className="sidebar-title">운영 분석</div><nav><a className="active" href="#where"><span>01</span> 집행 분포</a><a href="#effect"><span>02</span> 효과 추정</a><a href="#evaluation"><span>03</span> 모델 검증</a><a href="#next"><span>04</span> 분석 요약</a></nav><div className="sidebar-bottom"><span className="live-dot" /> 분석 모델<br /><strong>{overview.scenario}</strong><Link href="/participant">← 참가자 화면</Link></div></aside>
    <section className="admin-content">
      <header className="admin-header"><div><h1>RunMile 정책 효과 분석</h1><p>2026 대구마라톤 연계 사업</p></div><div className="admin-controls"><div className="scenario-control"><span>정책 강도</span><div className="scenario-selector" aria-label="정책 시나리오">{scenarios.map((item) => <button key={item.value} type="button" className={scenario === item.value ? "selected" : ""} aria-pressed={scenario === item.value} disabled={loading} onClick={() => setScenario(item.value)}>{item.label}</button>)}</div><small>{loading ? "분석 결과 갱신 중" : `${overview.scenario} 시나리오`}</small></div><div className="report-date"><span>분석 기준일</span><b>{reportDate || "-"}</b></div></div></header>
      {error && <div className="admin-error" role="alert"><span>{error}</span><button type="button" onClick={retryFailedRequests}>다시 시도</button></div>}
      <section className="overview-grid" aria-label="정책 분석 요약"><article><span>배정 예산</span><strong>{compactWon(overview.runmileBudget)}</strong><small>{formatWon(overview.runmileBudget)}</small></article><article><span>RunMile 집행액</span><strong>{compactWon(overview.runmileUsed)}</strong><small>{overview.runmileBudget === 0 ? "예산 집행 없음" : `예산 집행률 ${Math.round((overview.runmileUsed / overview.runmileBudget) * 100)}%`}</small></article><article className="linked"><span>연계 소비 총액</span><strong>{compactWon(overview.linkedPaymentAmount)}</strong><small>RunMile 포함 결제액</small></article><article className="impact"><span>추정 추가 소비액</span><strong>{compactWon(overview.estimatedIncrementalSales)}</strong><small>예산 대비 효과 {formatEffectRatio(overview.effectRatio)}</small></article></section>

      <section className="dashboard-section" id="where"><div className="dashboard-heading"><div><p className="section-kicker">지역·업종 집행 분석</p><h2>RunMile 사용 분포</h2></div></div>
        <div className="data-grid"><article className="chart-card"><h3>구별 RunMile 사용액</h3><div className="chart-body">{districts.map((item) => <Bar key={item.district} label={item.district} value={item.runmileUsed} max={maxDistrict} detail={`${item.transactionCount.toLocaleString()}건 · ${item.merchantCount}개 가맹점`} />)}</div></article>
          <article className="chart-card"><h3>업종별 연계 결제</h3><div className="chart-body">{categories.map((item) => <Bar key={item.category} label={merchantCategoryLabel[item.category]} value={item.linkedPaymentAmount} max={maxCategory} detail={`${item.transactionCount.toLocaleString()}건`} tone="blue" />)}</div></article></div>
        <article className="district-table-card"><header><h3>구·군별 소비 현황</h3><span>{districts.length}개 구·군</span></header><div className="district-table-scroll"><table><thead><tr><th>구·군</th><th>RunMile 사용액</th><th>연계 소비액</th><th>개인 결제액</th><th>결제 건수</th><th>가맹점</th></tr></thead><tbody>{districts.map((item) => <tr key={item.district}><th>{item.district}</th><td>{formatWon(item.runmileUsed)}</td><td>{formatWon(item.linkedPaymentAmount)}</td><td>{formatWon(item.personalPaymentAmount)}</td><td>{item.transactionCount.toLocaleString()}건</td><td>{item.merchantCount.toLocaleString()}개</td></tr>)}</tbody><tfoot><tr><th>전체</th><td>{formatWon(districts.reduce((sum, item) => sum + item.runmileUsed, 0))}</td><td>{formatWon(districts.reduce((sum, item) => sum + item.linkedPaymentAmount, 0))}</td><td>{formatWon(districts.reduce((sum, item) => sum + item.personalPaymentAmount, 0))}</td><td>{districts.reduce((sum, item) => sum + item.transactionCount, 0).toLocaleString()}건</td><td>{districts.reduce((sum, item) => sum + item.merchantCount, 0).toLocaleString()}개</td></tr></tfoot></table></div></article>
      </section>

      <section className="dashboard-section effect-section" id="effect"><div className="dashboard-heading"><div><p className="section-kicker">AI 효과 추정</p><h2>정책 효과 추정 결과</h2></div></div>
        <div className="effect-layout"><article className="effect-hero"><span>추정 추가 소비액</span><strong>{compactWon(overview.estimatedIncrementalSales)}<small>원</small></strong><div><b>{overview.effectRatio == null ? "산정 불가" : `${overview.effectRatio.toFixed(2)}×`}</b><span>예산 대비 효과비율</span></div></article><article className="effect-method"><h3>효과 산출 기준</h3><div><span>01</span><p><b>기준 매출 예측</b></p></div><div><span>02</span><p><b>비교군 보정</b></p></div><div><span>03</span><p><b>추정 추가 소비 산출</b></p></div>{topEffect && <footer><b>{topEffect.scopeValue} 추정 추가 소비</b> <strong>{formatWon(topEffect.estimatedIncrementalSales)}</strong></footer>}</article></div>
        <p className="method-note">* 시뮬레이션 데이터와 비교군 보정에 기반한 AI 추정치이며, 실제 정책 성과를 확정적으로 의미하지 않습니다.</p>
      </section>

      <section className="dashboard-section evaluation-section" id="evaluation"><div className="dashboard-heading"><div><p className="section-kicker">모델 검증</p><h2>AI 평가 지표</h2></div></div>
        {evaluation.baseline ? <><div className="metric-grid"><article><span>MAE</span><strong>{formatWon(Math.round(evaluation.baseline.mae))}</strong><small>평균 절대 오차</small></article><article><span>MAPE</span><strong>{(evaluation.baseline.mape * 100).toFixed(1)}%</strong><small>평균 절대 백분율 오차</small></article><article><span>RMSE</span><strong>{formatWon(Math.round(evaluation.baseline.rmse))}</strong><small>큰 오차에 가중된 지표</small></article></div>
          <div className="evaluation-table"><table><thead><tr><th>시나리오</th><th>주입 효과</th><th>추정 효과</th><th>추정 오차</th><th>오차율</th></tr></thead><tbody>{evaluation.effects.map((item) => <tr key={item.scenario}><th>{item.scenario}</th><td>{formatWon(item.injectedEffect)}</td><td>{formatWon(item.estimatedEffect)}</td><td>{formatWon(item.difference)}</td><td>{item.differencePct == null ? "산정 불가" : `${item.differencePct.toFixed(1)}%`}</td></tr>)}</tbody></table></div></> : <p className="evaluation-empty">AI 배치를 실행하면 모델 검증 지표가 표시됩니다.</p>}
        <p className="evaluation-note">합성 데이터에 주입한 효과와 모델 추정값을 비교한 검증 지표입니다. 실제 정책 성과를 의미하지 않습니다.</p>
      </section>

      <section className="dashboard-section next-section" id="next"><div className="dashboard-heading"><div><p className="section-kicker">분석 요약</p><h2>집행 데이터 주요 결과</h2></div></div><div className="insight-grid">{insights.map((insight, index) => <article key={`${insight.type}-${index}`}><span className="insight-index">분석 {String(index + 1).padStart(2, "0")}</span><h3>{insight.title}</h3><p>{insight.description}</p></article>)}</div></section>
      <footer className="admin-footer">시뮬레이션 데이터 기반 프로토타입 분석</footer>
    </section>
  </main>;
}
