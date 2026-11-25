import { useMemo, useState } from 'react'

export function useOptimisticReaction(resource, userId) {
  const scope = useMemo(() => new Set(), [resource, userId])
  const [state, setState] = useState(null)

  function reactionFor(id, active, count = 0) {
    const change = state?.scope === scope ? state.reactions.get(id) : undefined
    if (change && (change.pending || change.active !== Boolean(active))) return change
    return { active: Boolean(active), count: count ?? 0, pending: false }
  }

  async function toggle(id, reaction, save) {
    if (scope.has(id)) return
    scope.add(id)

    const change = {
      active: !reaction.active,
      count: Math.max(0, reaction.count + (reaction.active ? -1 : 1)),
      pending: true,
    }
    setState(previous => {
      const reactions = new Map(previous?.scope === scope ? previous.reactions : undefined)
      reactions.set(id, change)
      return { scope, reactions }
    })

    function finish(value) {
      setState(previous => {
        if (previous?.scope !== scope || previous.reactions.get(id) !== change) return previous
        const reactions = new Map(previous.reactions)
        reactions.set(id, { ...value, pending: false })
        return { scope, reactions }
      })
    }

    try {
      const saved = await save(change.active)
      finish(saved ?? change)
    } catch (error) {
      finish(reaction)
      alert(error.message)
    } finally {
      scope.delete(id)
    }
  }

  return { reactionFor, toggle }
}
