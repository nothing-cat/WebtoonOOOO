import {request,query} from './apiClient.js'
const path = id => '/webtoons/'+encodeURIComponent(id)
export const searchWebtoons = (filters={}) => request('/webtoons'+query(filters))
export const fetchPopularWebtoons = async (filters={}) => {
  const rows = await request('/webtoons/popular'+query(filters));
  return rows.filter(w => w.likeCount > 0).sort((a,b) => b.likeCount-a.likeCount || String(a.id).localeCompare(String(b.id)));
}
export const fetchWebtoonById = id => request(path(id))
export const fetchReviews = id => request(path(id)+'/reviews')
export const fetchGenres = () => request('/genres')
export const fetchPlatforms = () => request('/platforms')
