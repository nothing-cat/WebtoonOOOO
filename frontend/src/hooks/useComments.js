import { useEffect, useState } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import * as api from '../services/commentApi.js'
import { useOptimisticReaction } from './useOptimisticReaction.js'

export function useComments(webtoonId) {
  const { currentUser } = useAuth()
  const [commentList, setCommentList] = useState([])
  const [isLoading, setLoading] = useState(true)
  const [version, setVersion] = useState(0)
  const empathy = useOptimisticReaction(webtoonId, currentUser?.id)

  function empathyFor(comment) {
    return empathy.reactionFor(comment.id, comment.isLiked, comment.empathyCount)
  }

  useEffect(() => {
    let active = true
    setLoading(true)

    api.fetchComments(webtoonId)
      .then(comments => {
        if (active) setCommentList(comments)
      })
      .catch(error => {
        if (active) alert(error.message)
      })
      .finally(() => {
        if (active) setLoading(false)
      })

    return () => {
      active = false
    }
  }, [webtoonId, version, currentUser?.id])

  async function runCommentAction(action) {
    if (!currentUser) {
      alert('로그인 후 이용해 주세요.')
      return false
    }

    try {
      await action()
      setVersion(previousVersion => previousVersion + 1)
      return true
    } catch (error) {
      alert(error.message)
      return false
    }
  }

  function toggleEmpathy(id) {
    if (!currentUser) {
      alert('로그인 후 이용해 주세요.')
      return
    }

    const comment = commentList.find(comment => comment.id === id)
    if (comment) {
      return empathy.toggle(id, empathyFor(comment), active => api.setCommentLike(id, active))
    }
  }

  return {
    commentList: commentList.map(comment => {
      const reaction = empathyFor(comment)
      if (!reaction.pending && reaction.active === comment.isLiked && reaction.count === comment.empathyCount) {
        return comment
      }
      return {
        ...comment,
        isLiked: reaction.active,
        empathyCount: reaction.count,
        empathyPending: reaction.pending,
      }
    }),
    isLoading,
    addComment: text => runCommentAction(() => api.createComment(webtoonId, text)),
    deleteComment: id => runCommentAction(() => api.deleteComment(id)),
    editComment: (id, text) => runCommentAction(() => api.updateComment(id, text)),
    toggleEmpathy,
  }
}
