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

package org.isoron.uhabits.models.sqlite;

import android.database.sqlite.*;
import android.support.annotation.*;

import com.activeandroid.*;
import com.activeandroid.query.*;

import org.isoron.uhabits.models.*;
import org.isoron.uhabits.models.sqlite.records.*;

import java.util.*;

/**
 * Implementation of a {@link HabitList} that is backed by SQLite.
 */
public class SQLiteHabitList extends HabitList
{
    private static SQLiteHabitList instance;

    private HashMap<Long, Habit> cache;

    private final SQLiteUtils<HabitRecord> sqlite;


    private SQLiteHabitList()
    {
        cache = new HashMap<>();
        sqlite = new SQLiteUtils<>(HabitRecord.class);
    }

    /**
     * Returns the global list of habits.
     * <p>
     * There is only one list of habit per application, corresponding to the
     * habits table of the SQLite database.
     *
     * @return the global list of habits.
     */
    public static SQLiteHabitList getInstance()
    {
        if (instance == null) instance = new SQLiteHabitList();
        return instance;
    }

    @Override
    public void add(@NonNull Habit habit)
    {
        if (cache.containsValue(habit))
            throw new IllegalArgumentException("habit already added");

        HabitRecord record = new HabitRecord();
        record.copyFrom(habit);
        record.position = countWithArchived();

        Long id = habit.getId();
        if (id == null) id = record.save();
        else record.save(id);

        habit.setId(id);
        cache.put(id, habit);
    }

    @Override
    public int countActive()
    {
        SQLiteDatabase db = Cache.openDatabase();
        SQLiteStatement st = db.compileStatement(
            "select count(*) from habits where archived = 0");
        return (int) st.simpleQueryForLong();
    }

    @Override
    public int countWithArchived()
    {
        SQLiteDatabase db = Cache.openDatabase();
        SQLiteStatement st = db.compileStatement("select count(*) from habits");
        return (int) st.simpleQueryForLong();
    }

    @Override
    @NonNull
    public List<Habit> getAll(boolean includeArchive)
    {
        List<HabitRecord> recordList;
        if (includeArchive)
        {
            String query = HabitRecord.SELECT + "order by position";
            recordList = sqlite.query(query, null);
        }
        else
        {
            String query = HabitRecord.SELECT + "where archived = 0 " +
                           "order by position";
            recordList = sqlite.query(query, null);
        }

        List<Habit> habits = new LinkedList<>();
        for (HabitRecord record : recordList)
        {
            Habit habit = getById(record.getId());
            if (habit == null)
                throw new RuntimeException("habit not in database");
            habits.add(habit);
        }

        return habits;
    }

    @Override
    @Nullable
    public Habit getById(long id)
    {
        if (!cache.containsKey(id))
        {
            HabitRecord record = HabitRecord.get(id);
            if (record == null) return null;

            Habit habit = new Habit();
            record.copyTo(habit);
            cache.put(id, habit);
        }

        return cache.get(id);
    }

    @Override
    @Nullable
    public Habit getByPosition(int position)
    {
        String query = HabitRecord.SELECT + "where position = ? limit 1";
        String params[] = { Integer.toString(position) };
        HabitRecord record = sqlite.querySingle(query, params);
        if (record != null) return getById(record.getId());
        return null;
    }

    @Override
    public int indexOf(@NonNull Habit h)
    {
        if (h.getId() == null) return -1;
        HabitRecord record = HabitRecord.get(h.getId());
        if (record == null) return -1;
        return record.position;
    }

    @Deprecated
    public void rebuildOrder()
    {
        List<Habit> habits = getAll(true);

        int i = 0;
        for (Habit h : habits)
        {
            HabitRecord record = HabitRecord.get(h.getId());
            if (record == null)
                throw new RuntimeException("habit not in database");

            record.position = i++;
            record.save();
        }

        update(habits);
    }

    @Override
    public void remove(@NonNull Habit habit)
    {
        if (!cache.containsKey(habit.getId()))
            throw new RuntimeException("habit not in cache");

        cache.remove(habit.getId());
        HabitRecord record = HabitRecord.get(habit.getId());
        if (record == null) throw new RuntimeException("habit not in database");
        record.cascadeDelete();
        rebuildOrder();
    }

    @Override
    public void reorder(Habit from, Habit to)
    {
        if (from == to) return;

        Integer toPos = indexOf(to);
        Integer fromPos = indexOf(from);

        if (toPos < fromPos)
        {
            new Update(HabitRecord.class)
                .set("position = position + 1")
                .where("position >= ? and position < ?", toPos, fromPos)
                .execute();
        }
        else
        {
            new Update(HabitRecord.class)
                .set("position = position - 1")
                .where("position > ? and position <= ?", fromPos, toPos)
                .execute();
        }

        HabitRecord record = HabitRecord.get(from.getId());
        if (record == null) throw new RuntimeException("habit not in database");
        record.position = toPos;
        record.save();

        update(from);

        getObservable().notifyListeners();
    }

    @Override
    public void update(List<Habit> habits)
    {
        for (Habit h : habits)
        {
            HabitRecord record = HabitRecord.get(h.getId());
            if (record == null)
                throw new RuntimeException("habit not in database");
            record.copyFrom(h);
            record.save();
        }
    }

}