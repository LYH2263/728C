const puppeteer = require('puppeteer');

async function testGameDetail() {
  const browser = await puppeteer.launch({
    headless: false,
    defaultViewport: { width: 1920, height: 1080 }
  });
  
  const page = await browser.newPage();
  
  console.log('========== 游戏详情页补充测试 ==========\n');
  
  try {
    // 直接访问一个游戏详情页
    console.log('访问游戏详情页: http://localhost:3000/game/1');
    await page.goto('http://localhost:3000/game/1', { waitUntil: 'networkidle0', timeout: 30000 });
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    const detailImages = await page.evaluate(() => {
      const images = Array.from(document.querySelectorAll('img'));
      return images.map(img => {
        const rect = img.getBoundingClientRect();
        const isSvg = img.src.endsWith('.svg');
        const isDisplayed = rect.width > 0 && rect.height > 0;
        const hasError = img.complete && !isSvg && img.naturalWidth === 0;
        
        // 尝试识别图片的用途
        let purpose = '未知';
        const imgClasses = img.className || '';
        const parent = img.parentElement;
        const parentClasses = parent ? parent.className : '';
        
        if (imgClasses.includes('banner') || parentClasses.includes('banner')) {
          purpose = '顶部横幅';
        } else if (imgClasses.includes('cover') || parentClasses.includes('cover')) {
          purpose = '封面';
        } else if (imgClasses.includes('screenshot') || parentClasses.includes('screenshot')) {
          purpose = '游戏截图';
        } else if (imgClasses.includes('carousel') || parentClasses.includes('carousel') || parentClasses.includes('swiper')) {
          purpose = '轮播图';
        }
        
        return {
          src: img.src,
          alt: img.alt || '(无 alt)',
          purpose,
          displayWidth: Math.round(rect.width),
          displayHeight: Math.round(rect.height),
          isSvg,
          isDisplayed,
          hasError,
          className: imgClasses
        };
      });
    });
    
    console.log(`\n找到 ${detailImages.length} 个图片:\n`);
    
    // 按用途分组显示
    const purposes = ['顶部横幅', '封面', '游戏截图', '轮播图', '未知'];
    purposes.forEach(purpose => {
      const imgs = detailImages.filter(img => img.purpose === purpose);
      if (imgs.length > 0) {
        console.log(`\n【${purpose}】(${imgs.length} 张)`);
        imgs.forEach(img => {
          const status = img.isDisplayed && !img.hasError ? '✅' : '❌';
          console.log(`  ${status} ${img.src.substring(img.src.lastIndexOf('/') + 1)}`);
          console.log(`     尺寸: ${img.displayWidth}x${img.displayHeight}px`);
          if (!img.isDisplayed || img.hasError) {
            console.log(`     问题: ${!img.isDisplayed ? '未显示' : '加载失败'}`);
          }
        });
      }
    });
    
    const errorCount = detailImages.filter(img => !img.isDisplayed || img.hasError).length;
    
    console.log('\n\n========== 检查结果 ==========');
    console.log(`总图片数: ${detailImages.length}`);
    console.log(`正常显示: ${detailImages.length - errorCount}`);
    console.log(`显示异常: ${errorCount}`);
    
    if (errorCount === 0) {
      console.log('\n✅ 游戏详情页图片全部正常显示');
    } else {
      console.log('\n❌ 游戏详情页存在图片显示问题');
    }
    
    console.log('\n浏览器将在 10 秒后关闭...');
    await new Promise(resolve => setTimeout(resolve, 10000));
    
  } catch (error) {
    console.error('测试出错:', error.message);
  }
  
  await browser.close();
}

testGameDetail().catch(console.error);
