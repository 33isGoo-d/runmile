"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getApi } from "@/lib/api";
import type { AnalyticsOverview, CategoryAnalytics, DistrictAnalytics, Insight, PolicyEffect } from "@/types/contracts";

const won = (value: number) => `${value.toLocaleString("ko-KR")}원`;
const compactWon = (value: number) => `${(value / 100000000).toFixed(value >= 100000000 ? 1 : 2)}억`;
const labels: Record<string, string> = { RESTAURANT: "음식점", CAFE: "카페", RETAIL: "소매", ACCOMMODATION: "숙박", OTHER: "기타" };

function Bar({ label, value, max, detail, tone = "mint" }: { label: string; value: number; max: number; detail: string; tone?: "mint" | "blue" }) {
  return <div className="bar-row"><div className="bar-label"><b>{label}</b><span>{detail}</span></div><div className="bar-track"><i className={tone} style={{ width: `${Math.max(8, (value / Math.max(max, 1)) * 100)}%` }} /></div><strong>{won(value)}</strong></div>;
}

export default function AdminPage() {
  const [overview, setOverview] = useState<AnalyticsOverview | null>(null);
  const [districts, setDistricts] = useState<DistrictAnalytics[]>([]);
  const [categories, setCategories] = useState<CategoryAnalytics[]>([]);
  const [effects, setEffects] = useState<PolicyEffect[]>([]);
  const [insights, setInsights] = useState<Insight[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    void Promise.all([
      getApi<AnalyticsOverview>("/admin/analytics/overview"), getApi<DistrictAnalytics[]>("/admin/analytics/districts"),
      getApi<CategoryAnalytics[]>("/admin/analytics/categories"), getApi<PolicyEffect[]>("/admin/analytics/effects"), getApi<Insight[]>("/admin/analytics/insights")
    ]).then(([nextOverview, nextDistricts, nextCategories, nextEffects, nextInsights]) => {
      setOverview(nextOverview); setDistricts(nextDistricts); setCategories(nextCategories); setEffects(nextEffects); setInsights(nextInsights);
    }).catch((loadError: unknown) => setError(loadError instanceof Error ? loadError.message : "분석 데이터를 불러오지 못했습니다."));
  }, []);

  if (error) return <main className="admin-shell"><p className="loading-screen">{error}</p></main>;
  if (!overview) return <main className="admin-shell"><p className="loading-screen">정책 분석 데이터 로딩 중</p></main>;
  const maxDistrict = Math.max(...districts.map((item) => item.runmileUsed));
  const maxCategory = Math.max(...categories.map((item) => item.linkedPaymentAmount));
  const topEffect = effects[0];

  return <main className="admin-shell">
    <aside className="admin-sidebar"><Link className="brand" href="/">RUN<span>MILE</span></Link><div className="sidebar-title">운영 분석</div><nav><a className="active" href="#where"><span>01</span> 집행 분포</a><a href="#effect"><span>02</span> 효과 추정</a><a href="#next"><span>03</span> 분석 요약</a></nav><div className="sidebar-bottom"><span className="live-dot" /> 분석 모델<br /><strong>{overview.scenario}</strong><Link href="/participant">← 참가자 화면</Link></div></aside>
    <section className="admin-content">
      <header className="admin-header"><div><h1>RunMile 정책 효과 분석</h1><p>2026 대구마라톤 연계 사업</p></div><div className="report-date"><span>분석 기준일</span><b>2026. 09. 17</b></div></header>
      <section className="overview-grid" aria-label="정책 분석 요약"><article><span>배정 예산</span><strong>{compactWon(overview.runmileBudget)}</strong><small>{won(overview.runmileBudget)}</small></article><article><span>RunMile 집행액</span><strong>{compactWon(overview.runmileUsed)}</strong><small>예산 집행률 {Math.round((overview.runmileUsed / overview.runmileBudget) * 100)}%</small></article><article className="linked"><span>연계 소비 총액</span><strong>{compactWon(overview.linkedPaymentAmount)}</strong><small>RunMile 포함 결제액</small></article><article className="impact"><span>추정 추가 소비액</span><strong>{compactWon(overview.estimatedIncrementalSales)}</strong><small>예산 대비 효과 {overview.effectRatio.toFixed(2)}배</small></article></section>

      <section className="dashboard-section" id="where"><div className="dashboard-heading"><div><p className="section-kicker">지역·업종 집행 분석</p><h2>RunMile 사용 분포</h2></div></div>
        <div className="data-grid"><article className="chart-card"><h3>구별 RunMile 사용액</h3><div className="chart-body">{districts.map((item) => <Bar key={item.district} label={item.district} value={item.runmileUsed} max={maxDistrict} detail={`${item.transactionCount.toLocaleString()}건 · ${item.merchantCount}개 가맹점`} />)}</div></article>
          <article className="chart-card"><h3>업종별 연계 결제</h3><div className="chart-body">{categories.map((item) => <Bar key={item.category} label={labels[item.category] ?? item.category} value={item.linkedPaymentAmount} max={maxCategory} detail={`${item.transactionCount.toLocaleString()}건`} tone="blue" />)}</div></article></div>
        <article className="district-table-card"><header><h3>구·군별 소비 현황</h3><span>{districts.length}개 구·군</span></header><div className="district-table-scroll"><table><thead><tr><th>구·군</th><th>RunMile 사용액</th><th>연계 소비액</th><th>개인 결제액</th><th>결제 건수</th><th>가맹점</th></tr></thead><tbody>{districts.map((item) => <tr key={item.district}><th>{item.district}</th><td>{won(item.runmileUsed)}</td><td>{won(item.linkedPaymentAmount)}</td><td>{won(item.personalPaymentAmount)}</td><td>{item.transactionCount.toLocaleString()}건</td><td>{item.merchantCount.toLocaleString()}개</td></tr>)}</tbody><tfoot><tr><th>전체</th><td>{won(districts.reduce((sum, item) => sum + item.runmileUsed, 0))}</td><td>{won(districts.reduce((sum, item) => sum + item.linkedPaymentAmount, 0))}</td><td>{won(districts.reduce((sum, item) => sum + item.personalPaymentAmount, 0))}</td><td>{districts.reduce((sum, item) => sum + item.transactionCount, 0).toLocaleString()}건</td><td>{districts.reduce((sum, item) => sum + item.merchantCount, 0).toLocaleString()}개</td></tr></tfoot></table></div></article>
      </section>

      <section className="dashboard-section effect-section" id="effect"><div className="dashboard-heading"><div><p className="section-kicker">AI 효과 추정</p><h2>정책 효과 추정 결과</h2></div></div>
        <div className="effect-layout"><article className="effect-hero"><span>추정 추가 소비액</span><strong>{compactWon(overview.estimatedIncrementalSales)}<small>원</small></strong><div><b>{overview.effectRatio.toFixed(2)}×</b><span>예산 대비 효과비율</span></div></article><article className="effect-method"><h3>효과 산출 기준</h3><div><span>01</span><p><b>기준 매출 예측</b></p></div><div><span>02</span><p><b>비교군 보정</b></p></div><div><span>03</span><p><b>추정 추가 소비 산출</b></p></div>{topEffect && <footer><b>{topEffect.scopeValue} 추정 추가 소비</b> <strong>{won(topEffect.estimatedIncrementalSales)}</strong></footer>}</article></div>
        <p className="method-note">* 시뮬레이션 데이터와 비교군 보정에 기반한 AI 추정치이며, 실제 정책 성과를 확정적으로 의미하지 않습니다.</p>
      </section>

      <section className="dashboard-section next-section" id="next"><div className="dashboard-heading"><div><p className="section-kicker">분석 요약</p><h2>집행 데이터 주요 결과</h2></div></div><div className="insight-grid">{insights.map((insight, index) => <article key={`${insight.type}-${index}`}><span className="insight-index">분석 {String(index + 1).padStart(2, "0")}</span><h3>{insight.title}</h3><p>{insight.description}</p></article>)}</div></section>
      <footer className="admin-footer">시뮬레이션 데이터 기반 프로토타입 분석</footer>
    </section>
  </main>;
}
