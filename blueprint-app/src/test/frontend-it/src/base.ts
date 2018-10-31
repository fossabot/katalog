export function setupTests() {
    require('expect-puppeteer').setDefaultOptions({timeout: 5000});
}