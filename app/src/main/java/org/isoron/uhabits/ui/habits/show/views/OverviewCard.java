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

package org.isoron.uhabits.ui.habits.show.views;

import android.content.*;
import android.support.annotation.*;
import android.util.*;
import android.widget.*;

import org.isoron.uhabits.*;
import org.isoron.uhabits.models.*;
import org.isoron.uhabits.tasks.*;
import org.isoron.uhabits.ui.common.views.*;
import org.isoron.uhabits.utils.*;

import butterknife.*;

public class OverviewCard extends HabitCard
{
    @NonNull
    private Cache cache;

    @BindView(R.id.scoreRing)
    RingView scoreRing;

    @BindView(R.id.scoreLabel)
    TextView scoreLabel;

    @BindView(R.id.monthDiffLabel)
    TextView monthDiffLabel;

    @BindView(R.id.yearDiffLabel)
    TextView yearDiffLabel;

    @BindView(R.id.title)
    TextView title;

    private int color;

    public OverviewCard(Context context)
    {
        super(context);
        init();
    }

    public OverviewCard(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init();
    }

    @Override
    protected void refreshData()
    {
        new RefreshTask().execute();
    }

    private String formatPercentageDiff(float percentageDiff)
    {
        return String.format("%s%.0f%%", (percentageDiff >= 0 ? "+" : "\u2212"),
            Math.abs(percentageDiff) * 100);
    }

    private void init()
    {
        inflate(getContext(), R.layout.show_habit_overview, this);
        ButterKnife.bind(this);
        cache = new Cache();

        if (isInEditMode()) initEditMode();
    }

    private void initEditMode()
    {
        color = ColorUtils.getAndroidTestColor(1);
        cache.todayScore = Score.MAX_VALUE * 0.6f;
        cache.lastMonthScore = Score.MAX_VALUE * 0.42f;
        cache.lastYearScore = Score.MAX_VALUE * 0.75f;
        refreshColors();
        refreshScore();
    }

    private void refreshColors()
    {
        scoreRing.setColor(color);
        scoreLabel.setTextColor(color);
        title.setTextColor(color);
    }

    private void refreshScore()
    {
        float todayPercentage = cache.todayScore / Score.MAX_VALUE;
        float monthDiff =
            todayPercentage - (cache.lastMonthScore / Score.MAX_VALUE);
        float yearDiff =
            todayPercentage - (cache.lastYearScore / Score.MAX_VALUE);

        scoreRing.setPercentage(todayPercentage);
        scoreLabel.setText(String.format("%.0f%%", todayPercentage * 100));

        monthDiffLabel.setText(formatPercentageDiff(monthDiff));
        yearDiffLabel.setText(formatPercentageDiff(yearDiff));

        int inactiveColor = InterfaceUtils.getStyledColor(getContext(),
            R.attr.mediumContrastTextColor);

        monthDiffLabel.setTextColor(monthDiff >= 0 ? color : inactiveColor);
        yearDiffLabel.setTextColor(yearDiff >= 0 ? color : inactiveColor);

        postInvalidate();
    }

    private class Cache
    {
        public float todayScore;

        public float lastMonthScore;

        public float lastYearScore;
    }

    private class RefreshTask extends BaseTask
    {
        @Override
        protected void doInBackground()
        {
            ScoreList scores = getHabit().getScores();

            long today = DateUtils.getStartOfToday();
            long lastMonth = today - 30 * DateUtils.millisecondsInOneDay;
            long lastYear = today - 365 * DateUtils.millisecondsInOneDay;

            cache.todayScore = (float) scores.getTodayValue();
            cache.lastMonthScore = (float) scores.getValue(lastMonth);
            cache.lastYearScore = (float) scores.getValue(lastYear);
        }

        @Override
        protected void onPostExecute(Void aVoid)
        {
            refreshScore();
            super.onPostExecute(aVoid);
        }

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            color = ColorUtils.getColor(getContext(), getHabit().getColor());
            refreshColors();
        }
    }
}
