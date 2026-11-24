import {request} from './apiClient.js'
const list = id => '/webtoons/'+encodeURIComponent(id)+'/comments'
const item = id => '/comments/'+encodeURIComponent(id)
export const fetchComments = id => request(list(id))
export const createComment = (id,text) => request(list(id),'POST',{text})
export const updateComment = (id,text) => request(item(id),'PUT',{text})
export const deleteComment = id => request(item(id),'DELETE')
export const setCommentLike = (id,liked) => request(item(id)+'/like',liked?'POST':'DELETE')
