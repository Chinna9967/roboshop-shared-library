#!groovy

def decidePipeline(Map configMap){
    application = configMap.get("application")
    // here we aregetiing nodejs vm
    switch(application) {
        case 'nodeJSVM':
            nodeJSVMCI(configMap)
            echo "application is NOdejs and VM based"
        break
        case 'javaVM':
            javaVMCI(configMap)
        break
        default:
            error "Un recognised application"
        break
    }
    // echo "i need to take decision based on map you sent"
}