package com.xattacker.util

import java.lang.ref.WeakReference
import java.util.*

class WeakReferenceList<T>
{
    private val _references: ArrayList<WeakReference<T>>?

    val isEmpty: Boolean get() = _references == null || _references.isEmpty()

    init
    {
        _references = ArrayList()
    }

    fun count(): Int
    {
        return _references?.size ?: 0
    }

    fun get(aIndex: Int): T?
    {
        return _references?.get(aIndex)?.get()
    }

    fun clear()
    {
        _references?.clear()
    }

    fun addReference(aReference: T?)
    {
        if (aReference != null && _references != null)
        {
            var listener: WeakReference<T>?
            var existed = false
            var i = 0

            while (i < _references.size)
            {
                listener = _references[i]

                if (listener.get() != null)
                {
                    if (listener.get() === aReference)
                    {
                        existed = true

                        break
                    }
                }
                else
                {
                    _references.removeAt(i)
                    i--
                }

                i++
            }

            if (!existed)
            {
                _references.add(WeakReference(aReference))
            }
        }
    }

    fun removeReference(aReference: T?)
    {
        if (aReference != null && _references != null && !_references.isEmpty())
        {
            var listener: WeakReference<T>?
            var i = 0

            while (i < _references.size)
            {
                listener = _references[i]

                if (listener.get() != null)
                {
                    if (listener.get() === aReference)
                    {
                        _references.remove(listener)

                        break
                    }
                }
                else
                {
                    _references.removeAt(i)
                    i--
                }

                i++
            }
        }
    }

    fun fetch(aVisitor: (T) -> Unit)
    {
        if (_references != null && !_references.isEmpty())
        {
            var i = 0

            while (i < _references.size)
            {
                val ref = _references[i].get()

                if (ref != null)
                {
                    aVisitor.invoke(ref)
                }
                else
                {
                    _references.removeAt(i)
                    i--
                }

                i++
            }
        }
    }
}
