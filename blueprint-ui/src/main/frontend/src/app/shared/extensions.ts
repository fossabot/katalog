function toMultiMap<K, T>(this: T[], keyExtractor: (value: T) => K): Map<K, T[]> {
  const result = new Map<K, T[]>();

  this.forEach(value => {
    const key = keyExtractor(value);
    if (!result.has(key)) {
      result.set(key, [value]);
    } else {
      result.get(key).push(value);
    }
  });

  return result;
}

interface Array<T> {
  toMultiMap<K>(this: T[], keyExtractor: (value: T) => K): Map<K, T[]>;
}

Array.prototype.toMultiMap = toMultiMap;
