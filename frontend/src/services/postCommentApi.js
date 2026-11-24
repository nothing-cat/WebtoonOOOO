import {request} from './apiClient.js'
const list = id => '/posts/'+encodeURIComponent(id)+'/comments'
const item = id => '/post-comments/'+encodeURIComponent(id)
export const fetchPostComments = id => request(list(id))
export const createPostComment = (id,content) => request(list(id),'POST',{content})
export const updatePostComment = (id,content) => request(item(id),'PUT',{content})
export const deletePostComment = id => request(item(id),'DELETE')
