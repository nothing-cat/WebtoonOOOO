import {request} from './apiClient.js'
export const setFavorite = (id,liked) => request('/webtoons/'+encodeURIComponent(id)+'/favorite',liked?'POST':'DELETE')
