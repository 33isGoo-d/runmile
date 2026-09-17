"use client";

import Link from "next/link";
import { useState } from "react";

export default function HomePage() {
  const [showHowToUse, setShowHowToUse] = useState(false);

  return (
    <main className="landing-shell">
      <nav className="topbar" aria-label="주요 메뉴">
        <Link className="brand" href="/">RUN<span>MILE</span></Link>
        <span className="demo-pill">2026 대구마라톤</span>
      </nav>
      <section className="hero">
        <h1>완주의 여운이<br /><em>대구의 소비</em>로 이어지도록.</h1>
        <div className="hero-actions"><Link className="button button-primary" href="/participant">내 RunMile 확인하기 <span>→</span></Link><button className="text-link" type="button" onClick={() => setShowHowToUse((visible) => !visible)} aria-expanded={showHowToUse}>어떻게 이용하나요? {showHowToUse ? "↑" : "↓"}</button></div>
      </section>
      {showHowToUse && <section className="journey-card" id="how-it-works" aria-label="RunMile 이용 과정">
        <div><span className="journey-number">01</span><strong>완주 인증</strong></div><span className="journey-line" />
        <div><span className="journey-number">02</span><strong>RunMile 지급</strong></div><span className="journey-line" />
        <div><span className="journey-number">03</span><strong>가맹점에서 사용</strong></div>
      </section>}
      <Link className="admin-fab" href="/admin" aria-label="관리자 정책 분석 대시보드로 이동"><span>▦</span> 관리자 분석</Link>
    </main>
  );
}

