const {setup: setupPuppeteer} = require('jest-environment-puppeteer');
const expectPuppeteer = require('expect-puppeteer');

module.exports = async function globalSetup() {
    // Needed for Travis CI
    expectPuppeteer.setDefaultOptions({timeout: 5000});

    await setupPuppeteer();
};