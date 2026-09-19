"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { postApi } from "@/lib/api";

const journey = [
  { number: "01", title: "완주 인증", description: "대구마라톤 완주 기록과 완주증명 확인" },
  { number: "02", title: "RunMile 지급", description: "완주 보상 10,000 RunMile" },
  { number: "03", title: "지역 소비", description: "대구 지역 가맹점 사용" },
  { number: "04", title: "정책 분석", description: "지역 소비 흐름과 정책 효과" }
];

export default function HomePage() {
  const router = useRouter();
  const [resetting, setResetting] = useState(false);
  const [resetError, setResetError] = useState<string | null>(null);

  const startDemo = async () => {
    setResetting(true);
    setResetError(null);
    try {
      await postApi<{ reset: boolean }>("/demo/reset", { confirmation: "RESET" });
      router.push("/participant");
    } catch (error) {
      setResetError(error instanceof Error ? error.message : "시연 상태를 초기화하지 못했습니다.");
    } finally {
      setResetting(false);
    }
  };

  return (
    <main className="landing-app-shell">
      <header className="landing-app-header app-common-header">
        <Link className="brand" href="/">RUN<span>MILE</span></Link>
        <span className="landing-event">2026 대구마라톤</span>
        <Link className="landing-profile" href="/participant" aria-label="참가자 화면">R</Link>
      </header>

      <section className="landing-app-content">
        <div className="landing-kicker"><i /> 완주자 리워드</div>
        <h1>달린 만큼 혜택으로,<br /><em>RunMile</em></h1>
        <p className="landing-lead">대구마라톤 완주 보상과 지역 가맹점 소비를 잇는 RunMile</p>

        <div className="landing-demo-actions">
          <button className="landing-primary-action" type="button" disabled={resetting} onClick={startDemo}>
            <span>{resetting ? "시연 준비 중" : "처음부터 시연하기"}</span><b>→</b>
          </button>
          <Link className="landing-continue-action" href="/participant">현재 상태 이어보기</Link>
          {resetError && <p className="landing-reset-error" role="alert">{resetError}</p>}
        </div>
        <section className="landing-journey" id="journey" aria-label="RunMile 이용 과정">
          <header><strong>이용 과정</strong><span>STEP 01 · 04</span></header>
          <ol>
            {journey.map((item) => (
              <li key={item.number}>
                <span>{item.number}</span>
                <div><b>{item.title}</b><small>{item.description}</small></div>
              </li>
            ))}
          </ol>
        </section>

        <Link className="landing-admin-link" href="/admin">
          <span className="landing-admin-icon">⌁</span>
          <span><b>관리자 분석</b><small>지역 소비와 정책 효과</small></span>
          <strong>›</strong>
        </Link>

        <aside className="landing-notice">
          <b>RunMile 안내</b>
          <p>대구마라톤 완주자 대상 지역소비 인센티브</p>
        </aside>
      </section>
    </main>
  );
}
