import Link from "next/link";

export default function HomePage() {
  return (
    <main className="mx-auto flex min-h-screen max-w-5xl flex-col justify-center gap-8 px-6">
      <section>
        <p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">RunMile MVP</p>
        <h1 className="mt-3 text-4xl font-bold text-slate-950">대구마라톤 완주 보상과 지역소비 효과 분석</h1>
        <p className="mt-4 max-w-2xl text-lg text-slate-600">
          참가자는 완주/NFT 검증 후 RunMile을 받고, 관리자는 배치 AI 분석으로 지역 소비 효과를 확인합니다.
        </p>
      </section>
      <nav className="flex flex-wrap gap-3">
        <Link className="rounded bg-emerald-700 px-4 py-2 font-semibold text-white" href="/participant">
          Participant
        </Link>
        <Link className="rounded border border-slate-300 px-4 py-2 font-semibold text-slate-900" href="/admin">
          Admin Dashboard
        </Link>
      </nav>
    </main>
  );
}

