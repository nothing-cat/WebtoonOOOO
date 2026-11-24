import {request,query} from './apiClient.js'
const path = id => '/webtoons/'+encodeURIComponent(id)
export const searchWebtoons = (filters={}) => request('/webtoons'+query(filters))
export const fetchPopularWebtoons = (filters={}) => request('/webtoons/popular'+query(filters))
export const fetchWebtoonById = id => request(path(id))
export const fetchReviews = id => request(path(id)+'/reviews')
export const fetchGenres = () => request('/genres')
export const fetchPlatforms = () => request('/platforms')
