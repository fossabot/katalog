describe('Auth', () => {
    beforeAll(async () => {
        await page.goto('http://localhost:8080');
    });

    it('can login', async () => {
        await expect(page).toFillForm('form[name="login"]', {
            username: 'user',
            password: 'user',
        });
        await expect(page).toClick('button', { text: 'Login' });
        await expect(page).toMatch('Welcome');
    });
});