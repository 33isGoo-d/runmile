import type { Merchant, MerchantCategory } from "@/types/contracts";

export const merchantCategoryLabel: Record<MerchantCategory, string> = {
  RESTAURANT: "음식점",
  CAFE: "카페",
  RETAIL: "쇼핑",
  ACCOMMODATION: "숙박",
  OTHER: "기타"
};

const merchantPriceRange: Record<MerchantCategory, { min: number; max: number; step: number }> = {
  RESTAURANT: { min: 12000, max: 80000, step: 500 },
  CAFE: { min: 4500, max: 30000, step: 100 },
  RETAIL: { min: 18000, max: 200000, step: 1000 },
  ACCOMMODATION: { min: 70000, max: 300000, step: 5000 },
  OTHER: { min: 10000, max: 100000, step: 500 }
};

export function merchantSuggestedAmount(merchant: Pick<Merchant, "id" | "merchantCode" | "category">): number {
  const seed = `${merchant.id}:${merchant.merchantCode}`;
  let hash = 2166136261;
  for (let index = 0; index < seed.length; index += 1) {
    hash ^= seed.charCodeAt(index);
    hash = Math.imul(hash, 16777619);
  }

  const { min, max, step } = merchantPriceRange[merchant.category];
  const slots = Math.floor((max - min) / step) + 1;
  return min + ((hash >>> 0) % slots) * step;
}

export const formatWon = (value: number) => `${value.toLocaleString("ko-KR")}원`;
