"use client";
import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { getApi } from "@/lib/api";
import { formatWon, merchantCategoryLabel } from "@/lib/presentation";
import type { AiEvaluation, AnalyticsOverview, CategoryAnalytics, DistrictAnalytics, Insight, PolicyEffect, Scenario } from "@/types/contracts";

type Stage = "WHERE" | "EFFECT" | "VERIFY" | "NEXT";
const stages: Array<{ key: Stage; title: string; description: string }> = [
  { key: "WHERE", title: "지역 소비", description: "어디에서 RunMile 소비가 발생했나" },
  { key: "EFFECT", title: "정책 효과", description: "정책이 추가로 만든 소비는 얼마인가" },
  { key: "VERIFY", title: "결과 신뢰도", description: "이 결과는 얼마나 신뢰할 수 있나" },
  { key: "NEXT", title: "정책 보완", description: "다음 정책 보완 방향" }
];
const scenarioLabels: Record<Scenario, string> = { NONE: "미지원", LOW: "낮음", MEDIUM: "중간", HIGH: "높음" };
const pieColors = ["#365f8d", "#c43b32", "#d7a74e", "#718b6f", "#866e9a", "#5e9b9b", "#cf7c68", "#8994a5", "#b1a06a"];

export default function AdminPage() {
  const [stage, setStage] = useState<Stage>("WHERE");
  const [scenario, setScenario] = useState<Scenario>("MEDIUM");
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [districts, setDistricts] = useState<DistrictAnalytics[]>([]);
  const [categories, setCategories] = useState<CategoryAnalytics[]>([]);
  const [effects, setEffects] = useState<PolicyEffect[]>([]);
  const [insights, setInsights] = useState<Insight[]>([]);
  const [evaluation, setEvaluation] = useState<AiEvaluation>({ baseline: null, effects: [] });
  const [loading, setLoading] = useState(true);
  const [staticError, setStaticError] = useState<string | null>(null);
  const [scenarioError, setScenarioError] = useState<string | null>(null);
  const [staticRetry, setStaticRetry] = useState(0);
  const [scenarioRetry, setScenarioRetry] = useState(0);

  useEffect(() => {
    let active = true;
    setStaticError(null);
    void Promise.all([
      getApi<CategoryAnalytics[]>("/admin/analytics/categories"),
      getApi<AiEvaluation>("/admin/analytics/evaluation")
    ]).then(([nextCategories, nextEvaluation]) => {
      if (!active) return;
      setCategories(nextCategories);
      setEvaluation(nextEvaluation);
    }).catch((error: unknown) => {
      if (active) setStaticError(error instanceof Error ? error.message : "공통 분석 데이터를 불러오지 못했습니다.");
    });
    return () => { active = false; };
  }, [staticRetry]);

  useEffect(() => {
    let active = true;
    const query = `?scenario=${scenario}`;
    setLoading(true);
    setScenarioError(null);
    setOverview(null);
    void Promise.all([
      getApi<AnalyticsOverview>(`/admin/analytics/overview${query}`),
      getApi<DistrictAnalytics[]>(`/admin/analytics/districts${query}`),
      getApi<PolicyEffect[]>(`/admin/analytics/effects${query}`),
      getApi<Insight[]>(`/admin/analytics/insights${query}`)
    ]).then(([nextOverview, nextDistricts, nextEffects, nextInsights]) => {
      if (!active) return;
      setOverview(nextOverview);
      setDistricts(nextDistricts);
      setEffects(nextEffects);
      setInsights(nextInsights);
    }).catch((error: unknown) => {
      if (active) setScenarioError(error instanceof Error ? error.message : "시나리오 분석 데이터를 불러오지 못했습니다.");
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
  const pie = useMemo(()=>{ const total=districts.reduce((sum,item)=>sum+item.runmileUsed,0); let cursor=0; const parts=districts.map((item,index)=>{const start=cursor;cursor+=total?item.runmileUsed/total*100:0;return `${pieColors[index%pieColors.length]} ${start}% ${cursor}%`;}); return { total, background:`conic-gradient(${parts.join(",")})` };},[districts]);
  if (loading&&!overview) return <main className="admin-shell"><p className="loading-screen">정책 효과 분석 결과를 불러오고 있습니다.</p></main>;
  if (!overview) return <main className="admin-shell"><section className="analysis-readiness"><p className="eyebrow">RUNMILE 정책 분석</p><h1>정책 효과 분석을<br/>준비하고 있습니다.</h1><p>{scenarioError ?? "분석 결과가 준비되면 지역 소비 흐름과 다음 정책 제안을 확인할 수 있습니다."}</p><button type="button" onClick={retryFailedRequests}>최신 분석 결과 불러오기</button></section></main>;
  const topEffect=effects[0];
  return <main className="admin-shell"><header className="admin-app-header app-common-header"><Link className="brand" href="/">RUN<span>MILE</span></Link><span className="admin-event">2026 대구마라톤</span></header><section className="admin-briefing"><header className="admin-briefing-header"><div><p className="eyebrow">정책 효과 분석</p><h1>RunMile 정책 효과 분석</h1></div><div className="scenario-area"><span>지원 수준을 바꾸어 정책 효과를 비교합니다.</span><div className="scenario-buttons">{(["NONE","LOW","MEDIUM","HIGH"] as Scenario[]).map(item=><button type="button" key={item} className={scenario===item?"active":""} disabled={loading} onClick={()=>setScenario(item)}>{scenarioLabels[item]}</button>)}</div></div></header>{error&&<div className="admin-error" role="alert"><span>{error}</span><button type="button" onClick={retryFailedRequests}>다시 시도</button></div>}<section className="analysis-conclusion"><span>이번 정책의 추정 추가 소비</span><strong>{formatWon(overview.estimatedIncrementalSales)}</strong><p>RunMile 연계 결제 전체가 아닌, 정책으로 인해 추가 발생한 소비를 추정한 결과입니다.</p></section><nav className="analysis-progress">{stages.map((item,index)=><button type="button" key={item.key} className={stage===item.key?"active":""} onClick={()=>setStage(item.key)}><span>{String(index+1).padStart(2,"0")}</span><b>{item.title}</b><small>{item.description}</small></button>)}</nav>
  {stage==="WHERE"&&<section className="analysis-panel"><h2>01 · 지역 소비</h2><p className="panel-lead">구·업종별 결제 분포</p><div className="district-visual"><div className="district-pie" style={{background:pie.background}} aria-label={`구별 RunMile 사용 비중, 총 ${formatWon(pie.total)}`} /><div className="district-legend">{districts.map((item,index)=><div key={item.district}><i style={{background:pieColors[index%pieColors.length]}}/><b>{item.district}</b><span>{pie.total?Math.round(item.runmileUsed/pie.total*100):0}%</span></div>)}</div></div><div className="analysis-grid"><article><h3>구별 RunMile 사용</h3>{districts.map(item=><div className="rank-row" key={item.district}><b>{item.district}</b><span>{formatWon(item.runmileUsed)}</span><small>{item.transactionCount.toLocaleString()}건 · {item.merchantCount}개 가맹점</small></div>)}</article><article><h3>업종별 연계 결제</h3>{categories.map(item=><div className="rank-row" key={item.category}><b>{merchantCategoryLabel[item.category]}</b><span>{formatWon(item.linkedPaymentAmount)}</span><small>{item.transactionCount.toLocaleString()}건</small></div>)}</article></div></section>}
  {stage==="EFFECT"&&<section className="analysis-panel"><h2>02 · 정책 효과</h2><p className="panel-lead">연계 결제와 추정 추가 소비</p><div className="effect-equation"><div><span>연계 결제</span><b>{formatWon(overview.linkedPaymentAmount)}</b></div><i>≠</i><div className="accent"><span>추정 추가 소비</span><b>{formatWon(overview.estimatedIncrementalSales)}</b></div></div>{topEffect&&<p className="analysis-note">{topEffect.scopeValue} 최대 추가 소비</p>}</section>}
  {stage==="VERIFY"&&<section className="analysis-panel"><h2>03 · 결과 신뢰도</h2><p className="panel-lead">합성 데이터 기반 정확도 검증</p>{evaluation.baseline?<div className="trust-cards"><article><span>평균 예측 차이</span><b>{formatWon(Math.round(evaluation.baseline.mae))}</b><small>예상 기준 매출과 실제 결과의 평균 차이</small></article><article><span>평균 추정 오차</span><b>{(evaluation.baseline.mape*100).toFixed(1)}%</b><small>결과를 해석할 때 참고하는 오차 범위</small></article><article><span>검증 상태</span><b>완료</b><small>시뮬레이션 데이터 검증 결과</small></article></div>:<p className="analysis-note">검증 결과 준비</p>}</section>}
  {stage==="NEXT"&&<section className="analysis-panel"><h2>04 · 정책 보완</h2><p className="panel-lead">다음 회차 지원 방향</p><div className="insight-list">{insights.map(item=><article key={item.title}><span>제안</span><h3>{item.title}</h3><p>{item.description}</p></article>)}</div></section>}</section></main>;
}
