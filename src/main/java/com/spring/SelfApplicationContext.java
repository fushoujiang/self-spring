package com.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SelfApplicationContext {

    private Class configClass;

    /**
     * key->BeanName
     * value->BeanDefinition
     */
    private Map<String,BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>() ;

    //Component到类优先处理BeanPostProcessor然后创建BeanPostProcessor对象，然后执行
    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();


    public SelfApplicationContext(Class configClass) {
        this.configClass = configClass;
        //扫描
        scan(configClass);

        for (String beanName :beanDefinitionMap.keySet()){
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName,beanDefinition);
                singletonObjects.put(beanName,bean);
            }

        }



    }

    private void scan(Class configClass) {
        if (configClass.isAnnotationPresent(ComponentScan.class)){
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String  path = componentScanAnnotation.value();
            path = path.replace(".","/");
            ClassLoader classLoader = SelfApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file= new File(resource.getFile());
            if (file.isDirectory()){
                for (File f : file.listFiles()){
                    String absolutePath = f.getAbsolutePath();
                    absolutePath = absolutePath.substring(absolutePath.indexOf("com"),absolutePath.indexOf(".class"));
                    absolutePath =  absolutePath.replace("/",".");
                    System.out.println(absolutePath);
                    try {
                        Class<?> clazz = classLoader.loadClass(absolutePath);
                        if (clazz.isAnnotationPresent(Component.class)){
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)){
                                BeanPostProcessor instance = (BeanPostProcessor) clazz.getConstructor().newInstance();
                                beanPostProcessorList.add(instance);
                            }else {
                                Component componentAnnotation = clazz.getAnnotation(Component.class);

                                String beanName = componentAnnotation.value();
                                if ("".equals(beanName)){
                                    //默认名称
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                if (clazz.isAnnotationPresent(Scope.class)){
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    String value = scopeAnnotation.value();
                                    beanDefinition.setScope(value);
                                }
                                beanDefinitionMap.put(beanName,beanDefinition);
                            }
                        }
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
            }

        }
    }

    private Object createBean(String beanName,BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getType();
        Object instance = null;
        try {
            //TODO 推断构造方法
            instance = clazz.getConstructor().newInstance();
            //设置对象属性
            for (Field field:clazz.getDeclaredFields()){
                if (field.isAnnotationPresent(Autowired.class)){
                    field.setAccessible(true);
                    //TODO 直接根据属性名称来获取bean
                    field.set(instance,getBean(field.getName()));
                }
            }
            //BeanPostProcessor before
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }
            //InitializingBean
            if (instance instanceof  InitializingBean){
               ((InitializingBean) instance).afterPropertiesSet();
            }
            //BeanPostProcessor after
            for (BeanPostProcessor beanPostProcessor:beanPostProcessorList){
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return instance;


    }

    public Object getBean(String  beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

       if (Objects.isNull(beanDefinition)){
           throw new NullPointerException();
       }
       if (beanDefinition.getScope().equals("singleton")){
           Object singletonBean =  singletonObjects.get(beanName);
           if (singletonBean == null){
               singletonBean  =createBean(beanName,beanDefinition);
               singletonObjects.put(beanName,beanDefinition);
           }
           return singletonBean;
       }else {
           Object protoTypeBean = createBean(beanName,beanDefinition);
           return protoTypeBean;
       }
    }
}
