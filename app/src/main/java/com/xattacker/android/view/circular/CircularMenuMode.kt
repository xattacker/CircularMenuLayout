package com.xattacker.android.view.circular

enum class CircularMenuMode private constructor(internal var value: Int)
{
    AUTO(1),
    MANUAL(2),
    STABLE(3);

    companion object
    {
        fun parse(aValue: Int): CircularMenuMode
        {
            for (mode in CircularMenuMode.values())
            {
                if (mode.value == aValue)
                {
                    return mode
                }
            }

            return STABLE
        }
    }
}
