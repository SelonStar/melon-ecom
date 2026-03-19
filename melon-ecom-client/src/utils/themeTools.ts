import { ElMessage } from 'element-plus'

/**
 * 数值限制到 0~255
 */
function clampByte(n: number): number {
  if (!Number.isFinite(n)) return 0
  n = Math.round(n)
  if (n < 0) return 0
  if (n > 255) return 255
  return n
}

/**
 * level 限制到 0~1
 */
function clampLevel(level: number): number {
  if (!Number.isFinite(level)) return 0
  if (level < 0) return 0
  if (level > 1) return 1
  return level
}

/**
 * hex颜色转rgb颜色
 * @param str 颜色值字符串（支持 "#RRGGBB" / "RRGGBB"）
 * @returns 返回 [r,g,b]；非法输入会提示并返回 null（不会返回 void，避免 TS 类型污染）
 */
export function hexToRgb(str: string): [number, number, number] | null {
  const reg: RegExp = /^#?[0-9A-Fa-f]{6}$/
  if (!reg.test(str)) {
    ElMessage.warning('输入错误的hex')
    return null
  }

  const s = str.replace('#', '')
  const hexs = s.match(/../g)
  if (!hexs || hexs.length !== 3) {
    ElMessage.warning('输入错误的hex')
    return null
  }

  const r = parseInt(hexs[0], 16)
  const g = parseInt(hexs[1], 16)
  const b = parseInt(hexs[2], 16)
  return [r, g, b]
}

/**
 * rgb颜色转Hex颜色
 * @param r 代表红色 (0~255)
 * @param g 代表绿色 (0~255)
 * @param b 代表蓝色 (0~255)
 * @returns 返回 "#RRGGBB"；非法输入会提示并返回 null
 */
export function rgbToHex(r: number, g: number, b: number): string | null {
  const isInt = (n: number) => Number.isFinite(n) && Number.isInteger(n)
  if (!isInt(r) || !isInt(g) || !isInt(b)) {
    ElMessage.warning('输入错误的rgb颜色值')
    return null
  }

  const rr = clampByte(r).toString(16).padStart(2, '0')
  const gg = clampByte(g).toString(16).padStart(2, '0')
  const bb = clampByte(b).toString(16).padStart(2, '0')
  return `#${rr}${gg}${bb}`
}

/**
 * 加深颜色值
 * @param color 颜色值字符串
 * @param level 加深的程度，限0-1之间
 * @returns 返回 "#RRGGBB"；非法输入会提示并返回 null
 */
export function getDarkColor(color: string, level: number): string | null {
  const reg: RegExp = /^#?[0-9A-Fa-f]{6}$/
  if (!reg.test(color)) {
    ElMessage.warning('输入错误的hex颜色值')
    return null
  }

  const lv = clampLevel(level)
  const rgb = hexToRgb(color)
  if (!rgb) return null

  const [r, g, b] = rgb
  const nr = clampByte(Math.floor(r * (1 - lv)))
  const ng = clampByte(Math.floor(g * (1 - lv)))
  const nb = clampByte(Math.floor(b * (1 - lv)))

  return rgbToHex(nr, ng, nb)
}

/**
 * 变浅颜色值
 * @param color 颜色值字符串
 * @param level 变浅的程度，限0-1之间
 * @returns 返回 "#RRGGBB"；非法输入会提示并返回 null
 */
export function getLightColor(color: string, level: number): string | null {
  const reg: RegExp = /^#?[0-9A-Fa-f]{6}$/
  if (!reg.test(color)) {
    ElMessage.warning('输入错误的hex颜色值')
    return null
  }

  const lv = clampLevel(level)
  const rgb = hexToRgb(color)
  if (!rgb) return null

  const [r, g, b] = rgb
  const nr = clampByte(Math.floor((255 - r) * lv + r))
  const ng = clampByte(Math.floor((255 - g) * lv + g))
  const nb = clampByte(Math.floor((255 - b) * lv + b))

  return rgbToHex(nr, ng, nb)
}

/**
 * Rgba / Rgb 转换 hex（忽略 alpha）
 * @param rgba 颜色字符串，如 "rgb(64,158,255)" / "rgba(64,158,255,0.8)"
 * @returns "#RRGGBB"；非法格式会抛错
 */
export function rgbaToHex(rgba: string): string {
  const regex = /^rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)(?:\s*,\s*([\d.]+))?\s*\)$/
  const match = rgba.match(regex)
  if (!match) {
    throw new Error('Invalid RGBA string')
  }

  const r = clampByte(parseInt(match[1], 10))
  const g = clampByte(parseInt(match[2], 10))
  const b = clampByte(parseInt(match[3], 10))

  // 这里不走 rgbToHex 的校验提示，因为 rgbaToHex 约定为“解析失败就抛错”
  const rr = r.toString(16).padStart(2, '0')
  const gg = g.toString(16).padStart(2, '0')
  const bb = b.toString(16).padStart(2, '0')
  return `#${rr}${gg}${bb}`
}