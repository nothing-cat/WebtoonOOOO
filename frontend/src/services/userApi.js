import {request} from './apiClient.js'
export const fetchCurrentUser = () => request('/users/me')
export const fetchFavorites = () => request('/users/me/favorites')
export const fetchMyComments = () => request('/users/me/comments')
export const fetchMyPosts = () => request('/users/me/posts')
export const fetchMyPostComments = () => request('/users/me/post-comments')
