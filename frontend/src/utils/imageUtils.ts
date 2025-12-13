/**
 * 图片处理工具类
 * 用于解决直链图片无法显示的问题，包括URL验证、图片加载检测和错误处理
 */

export interface ImageValidationResult {
  valid: boolean;
  url: string;
  error?: string;
}

/**
 * 验证图片URL格式
 * @param url 图片URL
 * @returns 验证结果
 */
export function validateImageUrl(url: string | undefined | null): ImageValidationResult {
  if (!url) {
    return {
      valid: false,
      url: '',
      error: 'URL为空'
    };
  }

  // 检查URL格式
  try {
    const parsedUrl = new URL(url);
    
    // 检查协议
    if (!['http:', 'https:'].includes(parsedUrl.protocol)) {
      return {
        valid: false,
        url,
        error: 'URL协议必须是http或https'
      };
    }
    
    // 检查是否为图片文件（通过扩展名）
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.svg'];
    const urlLower = url.toLowerCase();
    const hasValidExtension = imageExtensions.some(ext => urlLower.endsWith(ext));
    
    // 检查Content-Type（如果URL包含查询参数）
    const hasImageContentType = urlLower.includes('image/');
    
    if (!hasValidExtension && !hasImageContentType) {
      // 允许没有扩展名但包含image/的URL（可能是动态生成的图片）
      if (!urlLower.includes('image/')) {
        console.warn(`图片URL可能不是有效的图片文件: ${url}`);
      }
    }
    
    return {
      valid: true,
      url
    };
  } catch (error) {
    return {
      valid: false,
      url,
      error: `URL格式无效: ${(error as Error).message}`
    };
  }
}

/**
 * 检测图片是否可以加载
 * @param url 图片URL
 * @returns Promise<boolean> 是否可以加载
 */
export function checkImageLoadable(url: string): Promise<boolean> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () => resolve(true);
    img.onerror = () => resolve(false);
    img.src = url;
  });
}

/**
 * 获取备用图片URL
 * @param width 宽度
 * @param height 高度
 * @param text 显示文本
 * @returns 备用图片的data URL
 */
export function getFallbackImage(width: number = 200, height: number = 200, text: string = '暂无图片'): string {
  return `data:image/svg+xml;charset=utf-8,%3Csvg xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2000%2Fsvg%22 width%3D%22${width}%22 height%3D%22${height}%22 viewBox%3D%220 0 ${width} ${height}%22%3E%3Crect width%3D%22${width}%22 height%3D%22${height}%22 fill%3D%22%23f5f5f5%22%2F%3E%3Ctext x%3D%2250%25%22 y%3D%2250%25%22 font-family%3D%22Arial%22 font-size%3D%2214%22 fill%3D%22%23999%22 text-anchor%3D%22middle%22 dy%3D%22.3em%22%3E${encodeURIComponent(text)}%3C%2Ftext%3E%3C%2Fsvg%3E`;
}

/**
 * 处理图片URL，确保可以安全加载
 * @param url 原始图片URL
 * @param fallbackText 备用文本
 * @returns 安全的图片URL
 */
export function processImageUrl(url: string | undefined | null, fallbackText: string = '暂无图片'): string {
  const validation = validateImageUrl(url);
  if (!validation.valid) {
    console.warn(`图片URL无效: ${validation.error}，使用备用图片`);
    return getFallbackImage(200, 200, fallbackText);
  }
  return validation.url;
}