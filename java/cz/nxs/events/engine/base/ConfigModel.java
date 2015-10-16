/*
 * Decompiled with CFR 0_102.
 * 
 * Could not load the following classes:
 *  cz.nxs.events.NexusLoader
 *  javolution.text.TextBuilder
 */
package cz.nxs.events.engine.base;

import javolution.text.TextBuilder;
import cz.nxs.events.NexusLoader;

public class ConfigModel
{
	String category;
	String key;
	String value;
	String desc;
	String defaultVal;
	InputType input;
	TextBuilder inputParams;
	
	public ConfigModel(String key, String value, String description)
	{
		this(key, value, description, value);
	}
	
	public ConfigModel(String key, String value, String description, String defaultVal)
	{
		this(key, value, description, defaultVal, InputType.TextEdit);
	}
	
	public ConfigModel(String key, String value, String description, InputType input)
	{
		this(key, value, description, value, input);
	}
	
	public ConfigModel(String key, String value, String description, String defaultVal, InputType input)
	{
		this(key, value, description, value, input, "");
	}
	
	public ConfigModel(String key, String value, String description, String defaultVal, InputType input, String inputParams)
	{
		this.key = key;
		this.value = value;
		this.desc = description;
		this.defaultVal = defaultVal;
		this.input = input;
		this.inputParams = new TextBuilder();
		if (this.input == InputType.Boolean)
		{
			this.addEnumOptions(new String[]
			{
				"True",
				"False"
			});
		}
		else
		{
			this.inputParams.append(inputParams);
		}
		this.category = "General";
	}
	
	public String encode()
	{
		return new TextBuilder().append(this.key + ":" + this.value + ";").toString();
	}
	
	public ConfigModel addEnumOptions(String[] options)
	{
		if ((this.input == InputType.Enum) || (this.input == InputType.Boolean))
		{
			int i = 1;
			for (String s : options)
			{
				this.inputParams.append(s);
				if (i != options.length)
				{
					this.inputParams.append(";");
				}
				++i;
			}
			return this;
		}
		NexusLoader.debug("can't add enum options to a non enum config model. (config key = " + this.key + ")");
		return this;
	}
	
	public ConfigModel setCategory(String cat)
	{
		this.category = cat;
		return this;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public void addToValue(String value)
	{
		if (this.input == InputType.MultiAdd)
		{
			this.value = this.value.length() > 0 ? this.value + "," + value : value;
		}
		else
		{
			NexusLoader.debug("can't add MultiAdd options to a non MultiAdd config model. (config key = " + this.key + ")");
		}
	}
	
	public String getKey()
	{
		return this.key;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public String getCategory()
	{
		return this.category;
	}
	
	public String removeMultiAddValueIndex(int index)
	{
		if (this.input == InputType.MultiAdd)
		{
			String[] values = this.value.split(",");
			for (int i = 0; i < values.length; ++i)
			{
				if (i != index)
				{
					continue;
				}
				values[i] = "";
				break;
			}
			String newValue = "";
			for (String v : values)
			{
				if (v.length() <= 0)
				{
					continue;
				}
				newValue = newValue + v + ",";
			}
			this.value = newValue.length() > 0 ? newValue.substring(0, newValue.length() - 1) : newValue;
			return this.value;
		}
		NexusLoader.debug("can't remove multiadd value by index from a non-MultiAdd config model. (config key = " + this.key + ")");
		return this.value;
	}
	
	public String getValueShownInHtml()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				try
				{
					String[] values = this.value.split(",");
					String toReturn = "";
					toReturn = toReturn + "<td width=240>";
					for (int i = 0; i < values.length; ++i)
					{
						toReturn = toReturn + "<font color=ac9887><a action=\"bypass -h admin_event_manage remove_multiadd_config_value " + i + "\">" + values[i] + "</a></font>";
						if ((i + 1) >= values.length)
						{
							continue;
						}
						toReturn = toReturn + " , ";
					}
					toReturn = toReturn + "</td>";
					return toReturn;
				}
				catch (Exception e)
				{
					return "<font color=4f4f4f>No values</font>";
				}
			}
		}
		return "<td width=240><font color=ac9887>" + this.value + "</font></td>";
	}
	
	public String getDesc()
	{
		return this.desc;
	}
	
	public String[] getValues()
	{
		if (this.input == InputType.MultiEdit)
		{
			return this.value.split(",");
		}
		NexusLoader.debug("can't call getValues() method for a non-MultiAdd config model. (config key = " + this.key + ")");
		return new String[]
		{
			this.value
		};
	}
	
	public String getDefaultVal()
	{
		return this.defaultVal;
	}
	
	public int getValueInt()
	{
		try
		{
			return Integer.parseInt(this.value);
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	public boolean getValueBoolean()
	{
		try
		{
			return Boolean.parseBoolean(this.value);
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	public String getInputHtml(int width)
	{
		return this.getInputHtml(width, 0);
	}
	
	public String getInputHtml(int width, int height)
	{
		switch (this.input)
		{
			case Enum:
			case Boolean:
			{
				return "<combobox width=" + width + " height=" + (height == 0 ? 17 : height) + " var=" + this.getKey() + " list=" + this.inputParams + ">";
			}
			case MultiEdit:
			{
				if (height > 0)
				{
					return "<multiedit var=" + this.getKey() + " width=" + width + " height=" + height + ">";
				}
				return "<multiedit var=" + this.getKey() + " width=" + width + ">";
			}
			case TextEdit:
			{
				if (height > 0)
				{
					return "<edit var=" + this.getKey() + " width=" + width + " height=" + height + ">";
				}
				return "<edit var=" + this.getKey() + " width=" + width + ">";
			}
			case MultiAdd:
			{
				if (height > 0)
				{
					return "<multiedit var=" + this.getKey() + " width=" + width + " height=" + height + ">";
				}
				return "<multiedit var=" + this.getKey() + " width=" + width + " height=15>";
			}
		}
		return "Input not available";
	}
	
	public String getAddButtonName()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				return "Add";
			}
		}
		return "Set";
	}
	
	public String getUtilButtonName()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				return "Remove all";
			}
		}
		return "Reset value to default";
	}
	
	public int getUtilButtonWidth()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				return 80;
			}
		}
		return 150;
	}
	
	public String getConfigHtmlNote()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				return "(click on a value to remove it)";
			}
		}
		return "Default: " + this.getDefaultVal();
	}
	
	public String getAddButtonAction()
	{
		switch (this.input)
		{
			case MultiAdd:
			{
				return "addto";
			}
		}
		return "set";
	}
	
	public InputType getInput()
	{
		return this.input;
	}
	
	public String getInputParams()
	{
		return this.inputParams.toString();
	}
	
	public static enum InputType
	{
		TextEdit,
		MultiEdit,
		Boolean,
		Enum,
		MultiAdd;
		
		private InputType()
		{
		}
	}
	
}
