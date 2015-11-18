package me.totalfreedom.totalfreedommod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.util.FUtil;
import net.pravian.aero.component.service.AbstractService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderSignal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Item;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Projectile;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class EntityWiper extends AbstractService<TotalFreedomMod>
{
    private static final long WIPE_RATE = 5 * 20L;
    //
    private final List<Class<? extends Entity>> wipables = new ArrayList<Class<? extends Entity>>();
    //
    private BukkitTask wipeTask;

    public EntityWiper(TotalFreedomMod plugin)
    {
        super(plugin);
        wipables.add(EnderCrystal.class);
        wipables.add(EnderSignal.class);
        wipables.add(ExperienceOrb.class);
        wipables.add(Projectile.class);
        wipables.add(FallingBlock.class);
        wipables.add(Firework.class);
        wipables.add(Item.class);
    }

    @Override
    protected void onStart()
    {
        if (!ConfigEntry.AUTO_ENTITY_WIPE.getBoolean())
        {
            return;
        }

        wipeTask = new BukkitRunnable()
        {

            @Override
            public void run()
            {
                wipeEntities(!ConfigEntry.ALLOW_EXPLOSIONS.getBoolean(), false);
            }
        }.runTaskTimer(plugin, WIPE_RATE, WIPE_RATE);

    }

    @Override
    protected void onStop()
    {
        FUtil.cancel(wipeTask);
        wipeTask = null;
    }

    public boolean canWipe(Entity entity, boolean wipeExplosives, boolean wipeVehicles)
    {
        if (wipeExplosives)
        {
            if (Explosive.class.isAssignableFrom(entity.getClass()))
            {
                return true;
            }
        }

        if (wipeVehicles)
        {
            if (Boat.class.isAssignableFrom(entity.getClass()))
            {
                return true;
            }
            else if (Minecart.class.isAssignableFrom(entity.getClass()))
            {
                return true;
            }
        }

        Iterator<Class<? extends Entity>> it = wipables.iterator();
        while (it.hasNext())
        {
            if (it.next().isAssignableFrom(entity.getClass()))
            {
                return true;
            }
        }

        return false;
    }

    public int wipeEntities(boolean wipeExplosives, boolean wipeVehicles)
    {
        int removed = 0;

        Iterator<World> worlds = Bukkit.getWorlds().iterator();
        while (worlds.hasNext())
        {
            Iterator<Entity> entities = worlds.next().getEntities().iterator();
            while (entities.hasNext())
            {
                Entity entity = entities.next();
                if (canWipe(entity, wipeExplosives, wipeVehicles))
                {
                    entity.remove();
                    removed++;
                }
            }
        }

        return removed;
    }

}