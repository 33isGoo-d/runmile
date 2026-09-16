import type { AnalyticsOverview } from "@/types/contracts";

const overview: AnalyticsOverview = {
  scenario: "MEDIUM",
  runmileBudget: 25000000,
  runmileUsed: 23100000,
  linkedPaymentAmount: 74200000,
  estimatedIncrementalSales: 37400000,
  effectRatio: 1.5
};

const formatWon = (value: number) => `${value.toLocaleString()}원`;

export default function AdminPage() {
  return (
    <main className="mx-auto min-h-screen max-w-6xl px-6 py-10">
      <h1 className="text-3xl font-bold text-slate-950">Admin Impact Dashboard</h1>
      <div className="mt-8 grid gap-4 md:grid-cols-3">
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="text-xl font-semibold text-slate-900">WHERE</h2>
          <p className="mt-2 text-sm text-slate-600">중구 RunMile 사용액</p>
          <p className="mt-3 text-2xl font-bold text-slate-950">{formatWon(8300000)}</p>
          <p className="mt-2 text-sm text-slate-600">연계 결제 {formatWon(25100000)} · 921건</p>
        </section>
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="text-xl font-semibold text-slate-900">EFFECT</h2>
          <p className="mt-2 text-sm text-slate-600">AI 추정 추가소비</p>
          <p className="mt-3 text-2xl font-bold text-emerald-700">{formatWon(overview.estimatedIncrementalSales)}</p>
          <p className="mt-2 text-sm text-slate-600">연계 총결제 {formatWon(overview.linkedPaymentAmount)}와 분리</p>
        </section>
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="text-xl font-semibold text-slate-900">NEXT</h2>
          <p className="mt-2 text-sm text-slate-600">소비 집중</p>
          <p className="mt-3 text-sm leading-6 text-slate-800">중구와 수성구에 전체 RunMile 소비의 52%가 집중되었습니다.</p>
        </section>
      </div>
      <section className="mt-4 rounded border border-slate-200 bg-white p-5">
        <h2 className="font-semibold text-slate-900">Overview · {overview.scenario}</h2>
        <div className="mt-4 grid gap-3 text-sm md:grid-cols-4">
          <p>Budget<br /><strong>{formatWon(overview.runmileBudget)}</strong></p>
          <p>Used<br /><strong>{formatWon(overview.runmileUsed)}</strong></p>
          <p>Linked Payment<br /><strong>{formatWon(overview.linkedPaymentAmount)}</strong></p>
          <p>Effect Ratio<br /><strong>{overview.effectRatio.toFixed(2)}x</strong></p>
        </div>
      </section>
    </main>
  );
}
