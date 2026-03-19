export interface ProductFormItemProps {
  productId?: number;
  name: string;
  subTitle?: string;
  categoryId?: number;
  mainImageUrl?: string;
  detailHtml?: string;
  status?: number;
  imageUrls?: string[];
  skus?: SkuFormItem[];
}

export interface SkuFormItem {
  skuId?: number;
  skuCode?: string;
  name?: string;
  specJson?: string;
  price?: number;
  costPrice?: number;
  stock?: number;
  weight?: number;
  imageUrl?: string;
  aiTryonEnabled?: number;
  tryonCategory?: string;
  tryonImageUrl?: string;
  tryonMaskUrl?: string;
  tryonSort?: number;
  status?: number;
}

export interface SpecAttribute {
  name: string;
  values: string[];
}
