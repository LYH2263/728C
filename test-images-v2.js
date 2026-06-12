const puppeteer = require('puppeteer');

async function testPages() {
  const browser = await puppeteer.launch({
    headless: false,
    defaultViewport: { width: 1920, height: 1080 }
  });
  
  const page = await browser.newPage();
  
  // 监听控制台消息
  page.on('console', msg => {
    const text = msg.text();
    if (!text.includes('ElementPlusError')) {
      console.log('PAGE LOG:', text);
    }
  });
  
  // 监听网络错误 (非 2xx 响应)
  const networkErrors = [];
  page.on('response', response => {
    const url = response.url();
    if (url.match(/\.(jpg|jpeg|png|gif|webp|svg)$/i)) {
      if (!response.ok()) {
        networkErrors.push({ url, status: response.status() });
      }
    }
  });
  
  const results = [];
  
  try {
    // 1. 测试首页
    console.log('\n=== 测试首页 ===');
    await page.goto('http://localhost:3000', { waitUntil: 'networkidle0', timeout: 30000 });
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    const homeImages = await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      return images.map(img => {
        const rect = img.getBoundingClientRect();
        const isSvg = img.src.endsWith('.svg');
        const isDisplayed = rect.width > 0 && rect.height > 0;
        const hasError = img.complete && !isSvg && img.naturalWidth === 0;
        
        return {
          src: img.src,
          alt: img.alt || '(无 alt)',
          complete: img.complete,
          naturalWidth: img.naturalWidth,
          naturalHeight: img.naturalHeight,
          displayWidth: Math.round(rect.width),
          displayHeight: Math.round(rect.height),
          isSvg,
          isDisplayed,
          hasError
        };
      });
    });
    
    const homeErrors = homeImages.filter(img => !img.isDisplayed || img.hasError);
    
    results.push({
      page: '首页',
      url: 'http://localhost:3000',
      totalImages: homeImages.length,
      errorImages: homeErrors,
      networkErrors: [...networkErrors],
      allImages: homeImages
    });
    
    console.log(`  找到 ${homeImages.length} 个图片`);
    console.log(`  显示正常: ${homeImages.filter(img => img.isDisplayed && !img.hasError).length}`);
    console.log(`  显示异常: ${homeErrors.length}`);
    
    networkErrors.length = 0;
    
    // 2. 测试商城页
    console.log('\n=== 测试商城页 ===');
    await page.goto('http://localhost:3000/store', { waitUntil: 'networkidle0', timeout: 30000 });
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    const storeImages = await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      return images.map(img => {
        const rect = img.getBoundingClientRect();
        const isSvg = img.src.endsWith('.svg');
        const isDisplayed = rect.width > 0 && rect.height > 0;
        const hasError = img.complete && !isSvg && img.naturalWidth === 0;
        
        return {
          src: img.src,
          alt: img.alt || '(无 alt)',
          complete: img.complete,
          naturalWidth: img.naturalWidth,
          naturalHeight: img.naturalHeight,
          displayWidth: Math.round(rect.width),
          displayHeight: Math.round(rect.height),
          isSvg,
          isDisplayed,
          hasError
        };
      });
    });
    
    const storeErrors = storeImages.filter(img => !img.isDisplayed || img.hasError);
    
    results.push({
      page: '商城页',
      url: 'http://localhost:3000/store',
      totalImages: storeImages.length,
      errorImages: storeErrors,
      networkErrors: [...networkErrors],
      allImages: storeImages
    });
    
    console.log(`  找到 ${storeImages.length} 个图片`);
    console.log(`  显示正常: ${storeImages.filter(img => img.isDisplayed && !img.hasError).length}`);
    console.log(`  显示异常: ${storeErrors.length}`);
    
    networkErrors.length = 0;
    
    // 3. 测试游戏详情页 (点击第一个游戏)
    console.log('\n=== 测试游戏详情页 ===');
    const firstGame = await page.$('a[href*="/game/"]');
    if (firstGame) {
      await firstGame.click();
      await new Promise(resolve => setTimeout(resolve, 3000));
      
      const detailImages = await page.evaluate(() => {
        const images = Array.from(document.querySelectorAll('img'));
        return images.map(img => {
          const rect = img.getBoundingClientRect();
          const isSvg = img.src.endsWith('.svg');
          const isDisplayed = rect.width > 0 && rect.height > 0;
          const hasError = img.complete && !isSvg && img.naturalWidth === 0;
          
          return {
            src: img.src,
            alt: img.alt || '(无 alt)',
            complete: img.complete,
            naturalWidth: img.naturalWidth,
            naturalHeight: img.naturalHeight,
            displayWidth: Math.round(rect.width),
            displayHeight: Math.round(rect.height),
            isSvg,
            isDisplayed,
            hasError
          };
        });
      });
      
      const detailErrors = detailImages.filter(img => !img.isDisplayed || img.hasError);
      
      results.push({
        page: '游戏详情页',
        url: page.url(),
        totalImages: detailImages.length,
        errorImages: detailErrors,
        networkErrors: [...networkErrors],
        allImages: detailImages
      });
      
      console.log(`  找到 ${detailImages.length} 个图片`);
      console.log(`  显示正常: ${detailImages.filter(img => img.isDisplayed && !img.hasError).length}`);
      console.log(`  显示异常: ${detailErrors.length}`);
      
      networkErrors.length = 0;
    }
    
    // 4. 登录
    console.log('\n=== 测试登录 ===');
    await page.goto('http://localhost:3000/login', { waitUntil: 'networkidle0', timeout: 30000 });
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    // 查找登录表单
    const loginSuccess = await page.evaluate(() => {
      const usernameInput = document.querySelector('input[name="username"]') || 
                            document.querySelector('input[type="text"]') ||
                            Array.from(document.querySelectorAll('input')).find(i => i.placeholder.includes('用户'));
      const passwordInput = document.querySelector('input[name="password"]') || 
                            document.querySelector('input[type="password"]');
      const loginButton = document.querySelector('button[type="submit"]') ||
                          Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('登录'));
      
      if (usernameInput && passwordInput && loginButton) {
        usernameInput.value = 'testuser';
        passwordInput.value = '123456';
        usernameInput.dispatchEvent(new Event('input', { bubbles: true }));
        passwordInput.dispatchEvent(new Event('input', { bubbles: true }));
        loginButton.click();
        return true;
      }
      return false;
    });
    
    if (loginSuccess) {
      await new Promise(resolve => setTimeout(resolve, 3000));
      console.log('  登录成功，当前 URL:', page.url());
      
      // 5. 测试愿望单
      console.log('\n=== 测试愿望单 ===');
      await page.goto('http://localhost:3000/wishlist', { waitUntil: 'networkidle0', timeout: 30000 });
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      const wishlistImages = await page.evaluate(() => {
        const images = Array.from(document.querySelectorAll('img'));
        return images.map(img => {
          const rect = img.getBoundingClientRect();
          const isSvg = img.src.endsWith('.svg');
          const isDisplayed = rect.width > 0 && rect.height > 0;
          const hasError = img.complete && !isSvg && img.naturalWidth === 0;
          
          return {
            src: img.src,
            alt: img.alt || '(无 alt)',
            complete: img.complete,
            naturalWidth: img.naturalWidth,
            naturalHeight: img.naturalHeight,
            displayWidth: Math.round(rect.width),
            displayHeight: Math.round(rect.height),
            isSvg,
            isDisplayed,
            hasError
          };
        });
      });
      
      const wishlistErrors = wishlistImages.filter(img => !img.isDisplayed || img.hasError);
      
      results.push({
        page: '愿望单',
        url: 'http://localhost:3000/wishlist',
        totalImages: wishlistImages.length,
        errorImages: wishlistErrors,
        networkErrors: [...networkErrors],
        allImages: wishlistImages
      });
      
      console.log(`  找到 ${wishlistImages.length} 个图片`);
      console.log(`  显示正常: ${wishlistImages.filter(img => img.isDisplayed && !img.hasError).length}`);
      console.log(`  显示异常: ${wishlistErrors.length}`);
      
      networkErrors.length = 0;
      
      // 6. 测试游戏库
      console.log('\n=== 测试游戏库 ===');
      await page.goto('http://localhost:3000/library', { waitUntil: 'networkidle0', timeout: 30000 });
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      const libraryImages = await page.evaluate(() => {
        const images = Array.from(document.querySelectorAll('img'));
        return images.map(img => {
          const rect = img.getBoundingClientRect();
          const isSvg = img.src.endsWith('.svg');
          const isDisplayed = rect.width > 0 && rect.height > 0;
          const hasError = img.complete && !isSvg && img.naturalWidth === 0;
          
          return {
            src: img.src,
            alt: img.alt || '(无 alt)',
            complete: img.complete,
            naturalWidth: img.naturalWidth,
            naturalHeight: img.naturalHeight,
            displayWidth: Math.round(rect.width),
            displayHeight: Math.round(rect.height),
            isSvg,
            isDisplayed,
            hasError
          };
        });
      });
      
      const libraryErrors = libraryImages.filter(img => !img.isDisplayed || img.hasError);
      
      results.push({
        page: '游戏库',
        url: 'http://localhost:3000/library',
        totalImages: libraryImages.length,
        errorImages: libraryErrors,
        networkErrors: [...networkErrors],
        allImages: libraryImages
      });
      
      console.log(`  找到 ${libraryImages.length} 个图片`);
      console.log(`  显示正常: ${libraryImages.filter(img => img.isDisplayed && !img.hasError).length}`);
      console.log(`  显示异常: ${libraryErrors.length}`);
      
      networkErrors.length = 0;
      
      // 7. 测试订单页
      console.log('\n=== 测试订单页 ===');
      await page.goto('http://localhost:3000/orders', { waitUntil: 'networkidle0', timeout: 30000 });
      await new Promise(resolve => setTimeout(resolve, 2000));
      
      const ordersImages = await page.evaluate(() => {
        const images = Array.from(document.querySelectorAll('img'));
        return images.map(img => {
          const rect = img.getBoundingClientRect();
          const isSvg = img.src.endsWith('.svg');
          const isDisplayed = rect.width > 0 && rect.height > 0;
          const hasError = img.complete && !isSvg && img.naturalWidth === 0;
          
          return {
            src: img.src,
            alt: img.alt || '(无 alt)',
            complete: img.complete,
            naturalWidth: img.naturalWidth,
            naturalHeight: img.naturalHeight,
            displayWidth: Math.round(rect.width),
            displayHeight: Math.round(rect.height),
            isSvg,
            isDisplayed,
            hasError
          };
        });
      });
      
      const ordersErrors = ordersImages.filter(img => !img.isDisplayed || img.hasError);
      
      results.push({
        page: '订单页',
        url: 'http://localhost:3000/orders',
        totalImages: ordersImages.length,
        errorImages: ordersErrors,
        networkErrors: [...networkErrors],
        allImages: ordersImages
      });
      
      console.log(`  找到 ${ordersImages.length} 个图片`);
      console.log(`  显示正常: ${ordersImages.filter(img => img.isDisplayed && !img.hasError).length}`);
      console.log(`  显示异常: ${ordersErrors.length}`);
    } else {
      console.log('  未能找到登录表单元素，跳过需要登录的页面测试');
    }
    
  } catch (error) {
    console.error('测试过程出错:', error.message);
  }
  
  // 输出详细结果
  console.log('\n\n========== 详细测试报告 ==========\n');
  
  let hasAnyIssue = false;
  
  results.forEach(result => {
    console.log(`\n【${result.page}】`);
    console.log(`URL: ${result.url}`);
    console.log(`图片总数: ${result.totalImages}`);
    console.log(`显示异常: ${result.errorImages.length}`);
    console.log(`网络错误: ${result.networkErrors.length}`);
    
    if (result.errorImages.length > 0) {
      console.log('\n异常图片详情:');
      result.errorImages.forEach(img => {
        console.log(`  - ${img.src.substring(img.src.lastIndexOf('/') + 1)}`);
        console.log(`    完整路径: ${img.src}`);
        console.log(`    显示尺寸: ${img.displayWidth}x${img.displayHeight}`);
        console.log(`    类型: ${img.isSvg ? 'SVG' : '位图'}`);
        console.log(`    状态: ${!img.isDisplayed ? '未显示' : '加载失败'}`);
      });
      hasAnyIssue = true;
    }
    
    if (result.networkErrors.length > 0) {
      console.log('\n网络请求错误:');
      result.networkErrors.forEach(err => {
        console.log(`  - ${err.url}`);
        console.log(`    HTTP 状态码: ${err.status}`);
      });
      hasAnyIssue = true;
    }
    
    if (result.errorImages.length === 0 && result.networkErrors.length === 0) {
      console.log('✅ 所有图片显示正常');
    } else {
      console.log('❌ 发现图片显示问题');
    }
  });
  
  console.log('\n\n========== 测试总结 ==========');
  console.log(`总测试页面数: ${results.length}`);
  console.log(`总图片数: ${results.reduce((sum, r) => sum + r.totalImages, 0)}`);
  console.log(`异常图片数: ${results.reduce((sum, r) => sum + r.errorImages.length, 0)}`);
  console.log(`网络错误数: ${results.reduce((sum, r) => sum + r.networkErrors.length, 0)}`);
  
  if (!hasAnyIssue) {
    console.log('\n✅ 验收通过：所有页面的图片均正常显示');
  } else {
    console.log('\n❌ 验收不通过：发现图片显示问题，请查看上述详情');
  }
  
  console.log('\n浏览器将在 15 秒后关闭，请查看实际页面效果...');
  await new Promise(resolve => setTimeout(resolve, 15000));
  await browser.close();
}

testPages().catch(console.error);
