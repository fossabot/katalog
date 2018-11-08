import './extensions';

describe('Extensions', () => {
  describe('toMultiMap', () => {
    it('should map an array to a multimap', () => {
      const input = [
        {id: 'a', value: 'foo'},
        {id: 'a', value: 'bar'},
        {id: 'b', value: 'baz'}
      ];

      const actual: Map<String, any[]> = input.toMultiMap(i => i.id);
      expect(actual.get('a')).toEqual([
        {id: 'a', value: 'foo'},
        {id: 'a', value: 'bar'}
      ]);
      expect(actual.get('b')).toEqual([
        {id: 'b', value: 'baz'}
      ]);
    });
  });
});
