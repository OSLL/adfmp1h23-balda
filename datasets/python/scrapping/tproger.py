from .scrap import get_soup, scrap_stuff
import os


lang = 'ru'


tags = {
    'for-beginners',
    'algorithms',
    'design-patterns',
    'software-development',
    'web',
    'gamedev',
    'mobiledev',
    'machine-learning',
    'neural-network',
    'ai',
    'data-science',
    'interface-design-ux',
    'sysadm',
    'python',
    'javascript',
    'golang',
    'java',
    'cpp',
    'c-sharp',
    'php',
    'css',
    'sql',
    'swift',
    'c-language',
}


def tag_url(tag):
    return f'https://tproger.ru/tag/{tag}/'


def articles_url():
    return 'https://tproger.ru/articles/'


def page_url(base_url, page: int):
    return f'{base_url}page/{page}/'


def all_urls(pages):
    for page in pages:
        yield 'all', page_url(articles_url(), page)
        for tag in tags:
            yield tag, page_url(tag_url(tag), page)


def section_url_dict(section_url):
    return {
        article_a.text.strip(): article_a.attrs['href']
        for article_a in get_soup(section_url).find_all('a', class_='article__link')
    }


def scrap_pages(pages, base_dir):
    url_dict = {
        article: (url, section)
        for section, section_url in all_urls(pages)
        for article, url in section_url_dict(section_url).items()
    }
    scrap_stuff(url_dict, base_dir, lang)
    # for section, url in all_urls(pages):
    #     scrap_stuff(section_url_dict(url), os.path.join(base_dir, section), lang)
