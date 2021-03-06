/*
 * Copyright (C) 2016 Álinson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.isoron.uhabits.ui;

import android.view.*;

import org.isoron.uhabits.tasks.*;

/**
 * Android implementation of {@link ProgressBar}.
 */
public class AndroidProgressBar implements ProgressBar
{
    private final android.widget.ProgressBar progressBar;

    public AndroidProgressBar(android.widget.ProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    @Override
    public void hide()
    {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void setTotal(int total)
    {
        if(total == 0)
            progressBar.setIndeterminate(true);
        else
        {
            progressBar.setIndeterminate(false);
            progressBar.setMax(total);
        }
    }

    @Override
    public void setCurrent(int current)
    {
        progressBar.setProgress(current);
    }

    @Override
    public void show()
    {
        progressBar.setVisibility(View.VISIBLE);
    }
}
