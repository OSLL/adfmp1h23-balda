from .scrap import get_soup, scrap_stuff
import os

dota2ru_base_url = "https://dota2.ru"

heroes_list_url = "/heroes"
items_list_url = "/items"
guides_list_url = "/guides"
tactics_list_url = "/tactics"


lang = 'ru'


def dota_url(url):
    return f'{dota2ru_base_url}{url}'


def scrap_heroes(base_dir):
    heroes_urls = {
        hero_a.attrs['data-title']: dota_url(hero_a.attrs['href'])
        for hero_a in get_soup(dota_url(heroes_list_url)).find_all("a", class_="base-hero__link-hero")
    }
    scrap_stuff(heroes_urls, os.path.join(base_dir, 'heroes'), lang)


def scrap_items(base_dir):
    items_urls = {
        item_a.parent.attrs['data-item-name']: dota_url(item_a.attrs['href'])
        for item_a in get_soup(dota_url(items_list_url)).find_all("a", class_="base-items__shop-link")
    }
    scrap_stuff(items_urls, os.path.join(base_dir, 'items'), lang)


def scrap_guides(base_dir):
    def guide_name(guide_a):
        return guide_a.find("div", class_="base-hero-hero__guid-top").find("div", class_=None).text

    guides_urls = {
        guide_name(guide_a): dota_url(guide_a.attrs['href'])
        for hero_guides_a in get_soup(dota_url(guides_list_url)).find_all("a", class_="base-hero__link-hero")
        for guide_a in get_soup(dota_url(hero_guides_a.attrs['href'])).find_all("a", class_="base-hero-hero__guid-link")
    }
    scrap_stuff(guides_urls, os.path.join(base_dir, 'guides'), lang)


def scrap_tactics(base_dir):
    def tactic_name(tactic_a):
        return tactic_a.find("div", class_="base-hero-hero__guid-top").find("div", class_=None).text

    tactics_urls = {
        tactic_name(tactic_a): dota_url(tactic_a.attrs['href'])
        for tactic_a in get_soup(dota_url(tactics_list_url)).find_all("a", class_="base-hero-hero__guid-link")
    }
    scrap_stuff(tactics_urls, os.path.join(base_dir, 'tactics'), lang)
    
    
def scrap_all(base_dir):
    def guide_name(guide_a):
        return guide_a.find("div", class_="base-hero-hero__guid-top").find("div", class_=None).text
    
    def tactic_name(tactic_a):
        return tactic_a.find("div", class_="base-hero-hero__guid-top").find("div", class_=None).text
    
    url_dict = dict()
    
    for hero_a in get_soup(dota_url(heroes_list_url)).find_all("a", class_="base-hero__link-hero"):
        url_dict[hero_a.attrs['data-title']] = (dota_url(hero_a.attrs['href']), 'heroes')
    
    for item_a in get_soup(dota_url(items_list_url)).find_all("a", class_="base-items__shop-link"):
        url_dict[item_a.parent.attrs['data-item-name']] = (dota_url(item_a.attrs['href']), 'items')
    
    for hero_guides_a in get_soup(dota_url(guides_list_url)).find_all("a", class_="base-hero__link-hero"):
        for guide_a in get_soup(dota_url(hero_guides_a.attrs['href'])).find_all("a", class_="base-hero-hero__guid-link"):
            url_dict[guide_name(guide_a)] = (dota_url(guide_a.attrs['href']), 'guides')
            
    for tactic_a in get_soup(dota_url(tactics_list_url)).find_all("a", class_="base-hero-hero__guid-link"):
        url_dict[tactic_name(tactic_a)] = (dota_url(tactic_a.attrs['href']), 'tactics')
        
    scrap_stuff(url_dict, base_dir, lang)
