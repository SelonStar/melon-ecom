import { request } from "@/utils/http";
import type { Result } from "@/types/api";

/** 分类 VO */
export type CategoryVO = {
  categoryId: number;
  parentId?: number;
  name: string;
  sort?: number;
  status?: number;
  children?: CategoryVO[];
};

/** 获取分类树（公开接口） */
export const getCategoryTree = (): Promise<Result<CategoryVO[]>> => {
  return request<CategoryVO[]>({
    method: "get",
    url: "/category/tree"
  });
};

/** 获取所有分类（管理端） */
export const getCategoryList = (): Promise<Result<CategoryVO[]>> => {
  return request<CategoryVO[]>({
    method: "get",
    url: "/admin/category/list"
  });
};
