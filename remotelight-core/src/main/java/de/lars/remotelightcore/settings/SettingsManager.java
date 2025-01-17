/*-
 * >===license-start
 * RemoteLight
 * ===
 * Copyright (C) 2019 - 2020 Lars O.
 * ===
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * <===license-end
 */

package de.lars.remotelightcore.settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tinylog.Logger;

import de.lars.remotelightcore.io.FileStorage;
import de.lars.remotelightcore.settings.types.SettingDouble;
import de.lars.remotelightcore.settings.types.SettingInt;
import de.lars.remotelightcore.settings.types.SettingObject;
import de.lars.remotelightcore.settings.types.SettingSelection;

public class SettingsManager {
	
	/**
	 * Intern + MusicEffect: Not displayed in settings UI
	 */
	public enum SettingCategory {
		General, Others, Intern, MusicEffect
	}
	
	private List<Setting> settings;
	private FileStorage fileStorage;
	
	public SettingsManager(FileStorage fileStorage) {
		this.settings = new ArrayList<Setting>();
		this.fileStorage = fileStorage;
	}
	
	/**
	 * Get all settings registered by this manager
	 * 
	 * @return A list with all settings
	 */
	public List<Setting> getSettings() {
		return this.settings;
	}
	
	/**
	 * Get a setting from type and id
	 * 
	 * @param <T>	the setting type
	 * @param type	the setting subclass
	 * @param id	the setting id
	 * @return		the setting or {@code null} if no setting with
	 * 				the given id could be found
	 */
	@SuppressWarnings("unchecked")
	public <T extends Setting> T getSetting(Class<T> type, String id) {
		Setting s = getSettingFromId(id);
		if(s != null && type.isInstance(s)) {
			return (T) s;
		}
		return null;
	}
	
	/**
	 * 
	 * @return A list with all settings from defined category
	 */
	public List<Setting> getSettingsFromCategory(SettingCategory category) {
		List<Setting> tmp = new ArrayList<Setting>();
		for(Setting s : settings) {
			if(s.getCategory() == category) {
				tmp.add(s);
			}
		}
		return tmp;
	}
	
	public Setting getSettingFromId(String id) {
		for(Setting s : settings) {
			if(s.getId().equals(id)) {
				return s;
			}
		}
		return null;
	}
	
	public SettingObject getSettingObject(String id) {
		return getSettingFromType(new SettingObject(id, null, null));
	}
	
	/**
	 * 
	 * @param type Setting subclass WITH defined ID
	 */
	@SuppressWarnings("unchecked")
	public <T extends Setting> T getSettingFromType(T type) {
		if(type.getId() != null) {
			return (T) getSettingFromId(type.getId());
		}
		return null;
	}
	
	/**
	 * Checks if setting with defined ID is registered
	 */
	public boolean isRegistered(String id) {
		return getSettingFromId(id) != null;
	}
	
	/**
	 * Register setting if not already registered
	 * @param setting new setting
	 */
	public <T extends Setting> T addSetting(T setting) {
		return addSetting(setting, true);
	}
	
	/**
	 * Register setting if not already registered
	 * @param setting new setting
	 * @param update add or remove options if available ({@link SettingSelection} only)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Setting> T addSetting(T setting, boolean update) {
		Setting existing = getSettingFromId(setting.getId());
		if(existing != null && setting.getClass().isInstance(existing)) {
			if(update)
				updateSetting(existing, setting);
			return (T) existing;
		} else if(existing != null) {
			Logger.info("Setting class of '{}' changed from {} to {}. Old setting instance will be removed.",
					existing.getId(), existing.getClass().getSimpleName(), setting.getClass().getSimpleName());
			removeSetting(existing.getId());
		}
		Logger.info("Registered Setting '" + setting.getId() + "'.");
		settings.add(setting);
		return setting;
	}
	
	/**
	 * Remove setting if available
	 */
	public void removeSetting(String id) {
		if(getSettingFromId(id) != null) {
			for(int i = 0; i < settings.size(); i++) {
				if(settings.get(i).getId().equals(id)) {
					settings.remove(i);
					Logger.info("Removed Setting '" + id + "'.");
					break;
				}
			}
		}
	}
	
	/**
	 * Deletes ALL settings
	 */
	public void deleteSettings() {
		settings = new ArrayList<Setting>();
	}
	
	public void updateSetting(Setting oldSetting, Setting newSetting) {
		// check if setting type has changed
		if(newSetting.getClass() != oldSetting.getClass()) {
			// update setting type
			int oldIndex = settings.indexOf(oldSetting);
			if(oldIndex != -1) {
				settings.set(oldIndex, newSetting);
				Logger.info("Updated setting " + oldSetting.getId() +
						". Old type: " + oldSetting.getClass().getSimpleName() +
						" New type: " + newSetting.getClass().getSimpleName());
			}
		// check if some data (name, description etc.; but not value) has changed
		} else if(oldSetting.getCategory() != newSetting.getCategory() ||
				!oldSetting.getDescription().equals(newSetting.getDescription()) ||
				!oldSetting.getName().equals(newSetting.getName())) {
			// update data
			oldSetting.setCategory(newSetting.getCategory());
			oldSetting.setDescription(newSetting.getDescription());
			oldSetting.setName(newSetting.getName());
			Logger.info("Updated setting " + oldSetting.getId());
		}
		// update selection values if both are from type SettingSelection
		if(oldSetting instanceof SettingSelection && newSetting instanceof SettingSelection) {
			updateSelectionValues((SettingSelection) oldSetting, (SettingSelection) newSetting);
		}
		// update min, max and step size values if both are from type SettingInt or SettingDouble
		if(oldSetting instanceof SettingInt && newSetting instanceof SettingInt) {
			SettingInt oldS = (SettingInt) oldSetting;
			SettingInt newS = (SettingInt) newSetting;
			oldS.setMin(newS.getMin());
			oldS.setMax(newS.getMax());
			oldS.setStepsize(newS.getStepsize());
		}
		if(oldSetting instanceof SettingDouble && newSetting instanceof SettingDouble) {
			SettingDouble oldS = (SettingDouble) oldSetting;
			SettingDouble newS = (SettingDouble) newSetting;
			oldS.setMin(newS.getMin());
			oldS.setMax(newS.getMax());
			oldS.setStepsize(newS.getStepsize());
		}
	}
	
	/**
	 * Update values of a {@link SettingSelection}
	 * @param oldSetting old setting that will be updated
	 * @param newSetting setting with new values
	 */
	public void updateSelectionValues(SettingSelection oldSetting, SettingSelection newSetting) {
		if(oldSetting == null || newSetting == null || !oldSetting.getId().equals(newSetting.getId()))
			return;
		if((oldSetting.getValues().length == 0 && newSetting.getValues().length == 0) ||
				Arrays.equals(oldSetting.getValues(), newSetting.getValues()))
			return;
		oldSetting.setValues(newSetting.getValues());
		// check if selected value was removed
		if(!Arrays.stream(oldSetting.getValues()).anyMatch(oldSetting.getSelected()::equals)) {
			if(Arrays.stream(oldSetting.getValues()).anyMatch(newSetting.getSelected()::equals)) {
				oldSetting.setSelected(newSetting.getSelected());
			} else {
				oldSetting.setSelected(oldSetting.getValues()[0]);
			}
		}
		Logger.info("Updated Setting '" + oldSetting.getId() + "'. New selection values: " + String.join(", ", newSetting.getValues()) + "; selected: " + oldSetting.getSelected());
	}
	
	/**
	 * Stores the settings in the data file
	 * @param key DataStorage Key
	 */
	public void save(String key) {
		fileStorage.store(key, settings);
		Logger.info("Stored " + settings.size() + " setting to data file.");
	}
	
	/**
	 * Loads settings from data file
	 * @param key DataStorage Key
	 */
	@SuppressWarnings("unchecked")
	public void load(String key) {
		if(fileStorage.get(key) != null && fileStorage.get(key) instanceof List<?>) {
			settings = (List<Setting>) fileStorage.get(key);
			Logger.info("Loaded " + settings.size() + " settings from data file.");
		} else {
			Logger.warn("Invalid or empty data! Could not load settings from data file.");
		}
	}
	
}
