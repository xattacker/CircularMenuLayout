package com.xattacker.util;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public final class WeakReferenceList <T>
{
	public interface WeakReferenceListVisitor <T>
	{
		void onReferenceFetched(T aReference);
	}
	
	
	private ArrayList<WeakReference<T>> _references;
	
	public WeakReferenceList()
	{
		_references = new ArrayList<WeakReference<T>>();
	}
	
	public boolean isEmpty()
	{
		return _references == null || _references.isEmpty();
	}

	public int size()
	{
		return _references != null ? _references.size() : 0;
	}

	public void clearReferences()
	{
		if (_references != null)
		{
			_references.clear();
		}
	}

	public T get(int aIndex) throws NullPointerException
	{
		return _references != null && _references.size() > aIndex ? _references.get(aIndex).get() : null;
	}

	public void addReference(T aReference)
	{
		if (aReference != null && _references != null)
		{
			WeakReference<T> listener = null;
			boolean existed = false;
			
			for (int i = 0; i < _references.size(); i++)
			{
				listener = _references.get(i);
				
				if (listener != null && listener.get() != null)
				{
					if (listener.get() == aReference)
					{
						existed = true;
					
						break;
					}
				}
				else
				{
					_references.remove(i);
					i--;
				}
			}
			
			if (!existed)
			{		
				_references.add(new WeakReference<T>(aReference));
			}
		}
	}
	
	public void removeReference(T aReference)
	{
		if (
			aReference != null &&
			_references != null &&
			!_references.isEmpty()
			)
		{
			WeakReference<T> listener = null;
			
			for (int i = 0; i < _references.size(); i++)
			{
				listener = _references.get(i);
				
				if (listener != null && listener.get() != null)
				{
					if (listener.get() == aReference)
					{
						_references.remove(listener);
					
						break;
					}
				}
				else
				{
					_references.remove(i);
					i--;
				}
			}
		}
	}
	
	public void fetchReference(WeakReferenceListVisitor<T> aVisitor)
	{
		if (aVisitor != null && _references != null && !_references.isEmpty())
		{
			 WeakReference<T> reference = null;
				
		     for (int i = 0; i < _references.size(); i++)
			 {
		   	 	 reference = _references.get(i);
				
				 if (reference != null && reference.get() != null)
				 {
					 aVisitor.onReferenceFetched(reference.get());
				 }
				 else
				 {
					 _references.remove(i);
					 i--;
				 }
			 }
		}
	}
}
