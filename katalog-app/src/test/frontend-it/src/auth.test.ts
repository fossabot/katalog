import {setupTests} from "./base";

setupTests();

describe('Auth', () => {
    beforeAll(async () => {
        await page.goto('http://localhost:8080');
    });

    it('can login', async () => {
        await expect(page).toFillForm('form[name="login"]', {
            username: 'user',
            password: 'user',
        });
        await expect(page).toClick('button[id="login"]');
        await expect(page).toMatch('user'); // the username in the topbar
    });
});