import type { MerchantCategory } from "@/types/contracts";

export const merchantCategoryLabel: Record<MerchantCategory, string> = {
  RESTAURANT: "음식점",
  CAFE: "카페",
  RETAIL: "쇼핑",
  ACCOMMODATION: "숙박",
  OTHER: "기타"
};

export const formatWon = (value: number) => `${value.toLocaleString("ko-KR")}원`;
