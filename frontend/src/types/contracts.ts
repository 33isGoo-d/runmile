export type Course = "FULL" | "TEN_K" | "FIVE_K";
export type RunMileTransactionType = "ISSUE" | "USE" | "CANCEL";
export type PaymentStatus = "SUCCESS" | "CANCELLED";
export type Scenario = "NONE" | "LOW" | "MEDIUM" | "HIGH";
export type MerchantCategory = "RESTAURANT" | "CAFE" | "RETAIL" | "ACCOMMODATION" | "OTHER";

export type Runner = {
  id: number;
  runnerCode: string;
  course: Course;
};

export type Completion = { completed: boolean; course: Course; finishTimeSeconds: number; completedAt: string };
export type NftRecord = { tokenId: string; network: string; verified: boolean };

export type Wallet = {
  balance: number;
  totalIssued: number;
  totalUsed: number;
};

export type RunMileIssueResponse = {
  issuedAmount: number;
  balance: number;
};

export type RunMileTransaction = {
  id: number;
  type: RunMileTransactionType;
  amount: number;
  paymentId: number | null;
  createdAt: string;
};

export type Merchant = {
  id: number;
  merchantCode: string;
  name: string;
  district: string;
  category: MerchantCategory;
  address: string;
  latitude: number | null;
  longitude: number | null;
  runmileEnabled: boolean;
};

export type Payment = {
  paymentId: number;
  totalAmount: number;
  runmileAmount: number;
  personalAmount: number;
  status: PaymentStatus;
};

export type AnalyticsOverview = {
  scenario: Scenario;
  runmileBudget: number;
  runmileUsed: number;
  linkedPaymentAmount: number;
  estimatedIncrementalSales: number;
  effectRatio: number | null;
};

export type DistrictAnalytics = {
  district: string;
  runmileUsed: number;
  linkedPaymentAmount: number;
  personalPaymentAmount: number;
  transactionCount: number;
  merchantCount: number;
};
export type CategoryAnalytics = {
  category: MerchantCategory;
  runmileUsed: number;
  linkedPaymentAmount: number;
  transactionCount: number;
};
export type PolicyEffect = {
  scopeType: "TOTAL" | "DISTRICT" | "CATEGORY";
  scopeValue: string;
  actualSales: number;
  predictedBaseline: number;
  estimatedIncrementalSales: number;
  effectRatio: number | null;
};
export type Insight = { type: string; title: string; description: string };

export type AiEvaluation = {
  baseline: {
    mae: number;
    mape: number;
    rmse: number;
    evaluatedAt: string;
  } | null;
  effects: Array<{
    scenario: Scenario;
    injectedEffect: number;
    estimatedEffect: number;
    difference: number;
    differencePct: number | null;
    evaluatedAt: string;
  }>;
};
