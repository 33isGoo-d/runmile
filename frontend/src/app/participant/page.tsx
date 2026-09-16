import type { Merchant, Runner, Wallet } from "@/types/contracts";

const runner: Runner = {
  id: 1,
  runnerCode: "RUNNER_00001",
  course: "FULL"
};

const wallet: Wallet = {
  balance: 10000,
  totalIssued: 10000,
  totalUsed: 0
};

const merchant: Merchant = {
  id: 10,
  merchantCode: "MERCHANT_00010",
  name: "RunMile 식당",
  district: "수성구",
  category: "RESTAURANT",
  address: "대구광역시 수성구",
  latitude: 35.84,
  longitude: 128.68,
  runmileEnabled: true
};

export default function ParticipantPage() {
  return (
    <main className="mx-auto min-h-screen max-w-5xl px-6 py-10">
      <h1 className="text-3xl font-bold text-slate-950">Participant</h1>
      <div className="mt-8 grid gap-4 md:grid-cols-2">
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="font-semibold text-slate-900">완주/NFT</h2>
          <p className="mt-2 text-sm text-slate-600">{runner.runnerCode} · {runner.course}</p>
          <p className="mt-4 text-xl font-bold text-emerald-700">NFT VERIFIED</p>
        </section>
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="font-semibold text-slate-900">RunMile Wallet</h2>
          <p className="mt-2 text-2xl font-bold text-slate-950">{wallet.balance.toLocaleString()} RunMile</p>
          <p className="mt-2 text-sm text-slate-600">
            Issued {wallet.totalIssued.toLocaleString()} · Used {wallet.totalUsed.toLocaleString()}
          </p>
        </section>
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="font-semibold text-slate-900">가맹점</h2>
          <p className="mt-2 text-lg font-bold text-slate-950">{merchant.name}</p>
          <p className="mt-1 text-sm text-slate-600">{merchant.district} · {merchant.category}</p>
          <p className="mt-1 text-sm text-slate-600">{merchant.address}</p>
        </section>
        <section className="rounded border border-slate-200 bg-white p-5">
          <h2 className="font-semibold text-slate-900">Payment Demo</h2>
          <dl className="mt-3 space-y-2 text-sm text-slate-700">
            <div className="flex justify-between"><dt>총 결제</dt><dd>35,000원</dd></div>
            <div className="flex justify-between"><dt>RunMile</dt><dd>10,000원</dd></div>
            <div className="flex justify-between"><dt>개인결제</dt><dd>25,000원</dd></div>
          </dl>
        </section>
      </div>
    </main>
  );
}
