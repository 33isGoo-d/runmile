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

export type Wallet = {
  balance: number;
  totalIssued: number;
  totalUsed: number;
};

export type Merchant = {
  id: number;
  merchantCode: string;
  name: string;
  district: string;
  category: MerchantCategory;
  address: string;
  latitude: number;
  longitude: number;
  runmileEnabled: boolean;
};

export type AnalyticsOverview = {
  scenario: Scenario;
  runmileBudget: number;
  runmileUsed: number;
  linkedPaymentAmount: number;
  estimatedIncrementalSales: number;
  effectRatio: number;
};
