import os
# import spacy
# import pymorphy3
# import functools
from joblib import Parallel, delayed, cpu_count
from tqdm_joblib import tqdm_joblib
from tqdm import tqdm


def files(path):
    if os.path.isfile(path):
        yield path
    for (d, _, fs) in os.walk(path):
        for f in fs:
            if not f.startswith('.'):
                yield os.path.join(d, f)
                
                
def words_in_file(file):
    with open(file, 'r') as f:
        for word in f:
            yield word.rstrip('\n')
            

def all_words(path):
    for file in files(path):
        yield from words_in_file(file)
        

def chunked(words, chunk_size):
    chunk = []
    for word in words:
        if len(chunk) >= chunk_size:
            yield chunk
            chunk = []
        chunk.append(word)
    yield chunk


def good_nouns(path, common_words, nlp):
    # @functools.cache
    # def nlp_cached(texts):
    #     return nlp(texts)
    
    total_cnt = sum(1 for _ in all_words(path))
    chunk_size = 10000
    print(f'{path}: {total_cnt} words to filter, {total_cnt/chunk_size} batches of size {chunk_size}')
    
    parallel = Parallel(n_jobs=cpu_count())

    def filter_words(words):
        result = []
        for word in words:
            if not (3 <= len(word) <= 25):
                continue
            if word in common_words:
                continue
    
            token = nlp(word)
            pos = token['pos']
            lemma = token['lemma']
    
            if lemma not in common_words and pos == 'NOUN':
                result.append(lemma)
        return result
        
    with tqdm_joblib(tqdm(desc=f"Noun filtering for {path}", total=total_cnt//chunk_size)) as _:
        yield from (
            word
            for words in parallel(
                delayed(filter_words)(words)
                for words in chunked(all_words(path), chunk_size)
            )
            for word in words
        )
    
    # with tempfile.NamedTemporaryFile('w') as tmp:
    #     print(f'writing filtered&lemmatized nouns of {path} to tmp file "{tmp.name}"')
    #     
    #     for i, word in enumerate(all_words(path)):
    #         print(f'\rnoun filtering for {path}: {i+1}/{total_cnt}', end='')
    #         if not (3 <= len(word) <= 25):
    #             continue
    #         if word in common_words:
    #             continue
    #         
    #         token = nlp_cached((word,))[0]
    #         pos = token['pos']
    #         lemma = token['lemma']
    #         
    #         if lemma not in common_words and pos == 'NOUN':
    #             tmp.write(lemma + '\n')
    #             yield lemma
    # print()


def counted(words):
    counts = dict()
    for noun in words:
        counts[noun] = counts.get(noun, 0) + 1
    return counts


def sorted_by_count(words):
    counts = list(counted(words).items())
    counts.sort(key=lambda noun_count: -noun_count[1])
    for (noun, cnt) in counts:
        yield noun + ',' + str(cnt)


# def ru_nlp():
#     morph = pymorphy3.MorphAnalyzer()
#     
#     def nlp(texts):
#         return [
#             {
#                 'lemma': token.normal_form,
#                 'pos': token.tag.POS,
#                 'original': token.word
#             }
#             for text in texts
#             for word in text.strip().split()
#             for token in morph.parse(word)
#         ]
#     return nlp


def process_words(src_path, dst_file, common_words, nlp, sort=True):
    words = good_nouns(src_path, common_words, nlp)
    if sort:
        words = sorted_by_count(words)

    with open(dst_file, 'w') as f:
        for i, word in enumerate(words):
            print(f'\rwriting to {dst_file}: {i + 1}', end='')
            f.write(word)
            f.write('\n')
    print()
        

# nlp = spacy.load('ru_core_news_md')
# print(list(nlp('итембилдов'))[0].lemma_)

# 155478
# 61493


# docs_path = os.path.join('..', 'docs')
# dota_path = os.path.join(docs_path, 'ru', 'dota2ru')
# prog_path = os.path.join(docs_path, 'ru', 'tproger')
# 
# with open(os.path.join('..', 'ru.txt'), 'r') as f:
#     common_ru_words = f.read()

# process_words(dota_path, os.path.join('..', 'ru-Дота.txt'), common_ru_words, ru_nlp())
# process_words(prog_path, os.path.join('..', 'ru-Программирование.txt'), common_ru_words, ru_nlp())

# writing filtered&lemmatized nouns of ../docs/ru/dota2ru to tmp file "/var/folders/75/x17tnjdj0dg683y6rphppbw00000kt/T/tmp9emtnu1k"
# noun filtering for ../docs/ru/dota2ru: 9504743/9504744
# writing to ../ru-Дота.txt: 40341
# writing filtered&lemmatized nouns of ../docs/ru/tproger to tmp file "/var/folders/75/x17tnjdj0dg683y6rphppbw00000kt/T/tmpiavdgw2d"
# noun filtering for ../docs/ru/tproger: 2906888/2906889
# writing to ../ru-Программирование.txt: 12412
# 
# writing filtered&lemmatized nouns of ../docs/ru/dota2ru to tmp file "/var/folders/75/x17tnjdj0dg683y6rphppbw00000kt/T/tmpjko_dkt9"
# noun filtering for ../docs/ru/dota2ru: 9504743/9504744
# writing to ../ru-Дота.txt: 21748
# writing filtered&lemmatized nouns of ../docs/ru/tproger to tmp file "/var/folders/75/x17tnjdj0dg683y6rphppbw00000kt/T/tmp7q35mef7"
# noun filtering for ../docs/ru/tproger: 2906888/2906889
# writing to ../ru-Программирование.txt: 2391

