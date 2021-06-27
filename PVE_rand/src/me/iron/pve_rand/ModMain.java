import api.config.BlockConfig;
import api.listener.events.controller.ClientInitializeEvent;
import api.listener.events.controller.ServerInitializeEvent;
import api.mod.ModSkeleton;
import api.mod.StarMod;
import api.mod.config.FileConfiguration;
import api.utils.particle.ModParticleUtil;
import org.schema.schine.resource.ResourceLoader;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 27.06.2021
 * TIME: 12:01
 */
public class ModMain extends StarMod {
    public static StarMod instance;
    public ModMain() {
        super();
    }

    @Override
    public ModSkeleton getSkeleton() {
        return super.getSkeleton();
    }

    @Override
    public byte[] onClassTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] byteCode) {
        return super.onClassTransform(loader, className, classBeingRedefined, protectionDomain, byteCode);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onServerCreated(ServerInitializeEvent event) {
        super.onServerCreated(event);
    }

    @Override
    public void onClientCreated(ClientInitializeEvent event) {
        super.onClientCreated(event);
    }

    @Override
    public FileConfiguration getConfig(String name) {
        return super.getConfig(name);
    }

    @Override
    public void addClassFileTransformer(ClassFileTransformer transformer) {
        super.addClassFileTransformer(transformer);
    }

    @Override
    public void onUniversalRegistryLoad() {
        super.onUniversalRegistryLoad();
    }

    @Override
    public void onBlockConfigLoad(BlockConfig config) {
        super.onBlockConfigLoad(config);
    }

    @Override
    public void onResourceLoad(ResourceLoader loader) {
        super.onResourceLoad(loader);
    }

    @Override
    public void onLoadModParticles(ModParticleUtil.LoadEvent event) {
        super.onLoadModParticles(event);
    }

    @Override
    public void setSkeleton(ModSkeleton skeleton) {
        super.setSkeleton(skeleton);
    }

    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public InputStream getJarResource(String url) throws IllegalArgumentException {
        return super.getJarResource(url);
    }

    @Override
    public BufferedImage getJarBufferedImage(String url) throws IllegalArgumentException {
        return super.getJarBufferedImage(url);
    }

    @Override
    protected void forceDefine(String name) {
        super.forceDefine(name);
    }
}
