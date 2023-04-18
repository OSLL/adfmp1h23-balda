from scrapping import dota2ru, tproger
from filter_nouns import process_words
import os
import pymorphy3
import nltk


def scrap_all():
    def dst_dir(topic, lang):
        return os.path.join('..', 'docs', lang, topic)
    
    dota_dir = dst_dir('dota2ru', dota2ru.lang)
    dota2ru.scrap_all(dota_dir)
    
    tproger_dir = dst_dir('tproger', tproger.lang)
    tproger.scrap_pages(range(1, 20), tproger_dir)
    

def filter_common_nouns():
    def en_nlp():
        nltk.download('averaged_perceptron_tagger')
        nltk.download('wordnet')
        wnl = nltk.stem.wordnet.WordNetLemmatizer()

        def nlp(word_and_count):
            word, count = word_and_count.strip().lower().split(",")
            pos = nltk.pos_tag([word])[0][1]
            if pos == 'NN':
                pos = 'n'
            lemma = wnl.lemmatize(word, pos) if pos == 'n' else word
            if pos == 'n':
                pos = 'NOUN'
            
            return {
                'lemma': f'{lemma},{count}',
                'pos': pos,
                'original': word_and_count
            }
        return nlp

    def ru_nlp():
        morph = pymorphy3.MorphAnalyzer()
    
        def nlp(word_and_count):
            word, count = word_and_count.strip().lower().split(",")
            token = morph.parse(word)[0]
            return {
                'lemma': f'{token.normal_form},{count}',
                'pos': token.tag.POS,
                'original': token.word
            }
        return nlp
    
    langs = {
        'ru': ru_nlp(),
        'en': en_nlp(),
    }
    
    for lang, nlp in langs.items():
        src_path = os.path.join('..', 'docs', lang, 'common.txt')
        dst_path = os.path.join('..', f'{lang}.txt')
        process_words(src_path, dst_path, set(), nlp, sort=False)

    
def filter_theme_nouns():
    def ru_nlp():
        morph = pymorphy3.MorphAnalyzer()

        def nlp(word):
            token = morph.parse(word.strip().lower())[0]
            return {
                'lemma': token.normal_form,
                'pos': token.tag.POS,
                'original': token.word
            }
        return nlp
    
    docs_path = os.path.join('..', 'docs')
    dota_path = os.path.join(docs_path, 'ru', 'dota2ru')
    prog_path = os.path.join(docs_path, 'ru', 'tproger')
    
    with open(os.path.join('..', 'ru.txt'), 'r') as f:
        common_ru_words = f.read()
    
    process_words(dota_path, os.path.join('..', 'ru-Дота.txt'), common_ru_words, ru_nlp())
    process_words(prog_path, os.path.join('..', 'ru-Программирование.txt'), common_ru_words, ru_nlp())


scrap_all()
filter_common_nouns()
filter_theme_nouns()
