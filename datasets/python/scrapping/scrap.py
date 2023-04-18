from bs4 import BeautifulSoup
import requests
import os
import re
from joblib import Parallel, delayed, cpu_count
from tqdm_joblib import tqdm_joblib
from tqdm import tqdm

alphabets = {
    'ru': 'абвгдеёжзийклмнопрстуфхцчшщъыьэюя',
    'en': 'abcdefghijklmnopqrstuvwxyz'
}


def escape_file_name(dir_name):
    return '_'.join(part for part in dir_name.split(os.path.sep) if part)


def get_soup(url):
    page = requests.get(url)
    return BeautifulSoup(page.content, 'html.parser')


def text(soup):
    return ' '.join(
        txt.strip().lower()
        for txt in soup.find_all(text=True)
    )


# dict is Map<ItemName, (Url, Dir)>
def scrap_stuff(url_dict: dict[str, (str, str)], base_dir, lang):
    if lang not in alphabets.keys():
        raise ValueError(lang)

    alphabet = alphabets[lang]

    def scrap_single(stuff, url, dst_dir, alphabet):
        os.makedirs(dst_dir, exist_ok=True)
        with open(os.path.join(dst_dir, escape_file_name(stuff)), 'w') as stuff_words_file:
            stuff_text = text(get_soup(url))
            words = re.findall(f'[{alphabet}]+', stuff_text)
            stuff_words_file.write('\n'.join(words))    

    parallel = Parallel(n_jobs=cpu_count())
    with tqdm_joblib(tqdm(desc=f"Scrapping into {base_dir}", total=len(url_dict))) as _:
        parallel(
            delayed(scrap_single)(stuff, url, os.path.join(base_dir, dst_dir), alphabet)
            for stuff, (url, dst_dir) in url_dict.items()
        )         
