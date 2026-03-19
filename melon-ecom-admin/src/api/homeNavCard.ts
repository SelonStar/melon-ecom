import { request } from "@/utils/http";
import type { Result } from "@/types/api";

export type HomeNavCardItemVO = {
  itemId?: number | null;
  keyword?: string | null;
  title?: string | null;
  imageUrl?: string | null;
  sort?: number | null;
};

export type HomeNavCardVO = {
  cardId?: number | null;
  title?: string | null;
  sort?: number | null;
  items?: HomeNavCardItemVO[];
};

export type HomeNavCardSaveDTO = {
  cards: Array<{
    cardId?: number | null;
    title: string;
    sort: number;
    items: Array<{
      itemId?: number | null;
      keyword: string;
      title: string;
      imageUrl: string;
      sort: number;
    }>;
  }>;
};

export const getHomeNavCards = (): Promise<Result<HomeNavCardVO[]>> => {
  return request<HomeNavCardVO[]>({
    method: "get",
    url: "/admin/homeNavCards"
  });
};

export const saveHomeNavCards = (
  data: HomeNavCardSaveDTO
): Promise<Result<any>> => {
  return request({
    method: "put",
    url: "/admin/homeNavCards",
    data
  });
};
