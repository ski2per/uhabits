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

package org.isoron.uhabits.ui.widgets;

import android.app.*;
import android.content.*;
import android.support.annotation.*;
import android.view.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.ui.common.views.*;
import org.isoron.uhabits.ui.habits.show.views.*;
import org.isoron.uhabits.ui.widgets.views.*;
import org.isoron.uhabits.utils.*;

import java.util.*;

public class ScoreWidget extends BaseWidget
{
    @NonNull
    private Habit habit;

    public ScoreWidget(@NonNull Context context, int id, @NonNull Habit habit)
    {
        super(context, id);
        this.habit = habit;
    }

    @Override
    public PendingIntent getOnClickPendingIntent(Context context)
    {
        return HabitPendingIntents.viewHabit(context, habit);
    }

    @Override
    public void refreshData(View view)
    {
        int defaultScoreInterval =
            InterfaceUtils.getDefaultScoreSpinnerPosition(getContext());
        int size = ScoreCard.BUCKET_SIZES[defaultScoreInterval];

        GraphWidgetView widgetView = (GraphWidgetView) view;
        ScoreChart chart = (ScoreChart) widgetView.getDataView();

        List<Score> scores;
        ScoreList scoreList = habit.getScores();

        if (size == 1) scores = scoreList.getAll();
        else scores = scoreList.groupBy(ScoreCard.getTruncateField(size));

        int color = ColorUtils.getColor(getContext(), habit.getColor());

        chart.setIsTransparencyEnabled(true);
        chart.setBucketSize(size);
        chart.setColor(color);
        chart.setScores(scores);
    }

    @Override
    protected View buildView()
    {
        ScoreChart dataView = new ScoreChart(getContext());
        GraphWidgetView view = new GraphWidgetView(getContext(), dataView);
        view.setTitle(habit.getName());
        return view;
    }

    @Override
    protected int getDefaultHeight()
    {
        return 300;
    }

    @Override
    protected int getDefaultWidth()
    {
        return 300;
    }
}
