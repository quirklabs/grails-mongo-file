class MongoFileGrailsPlugin {
    def version = "1.4.4"
    def grailsVersion = "2.4 > *"
    def dependsOn = [:]
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "MongoFile Plugin"
    def author = "Craig Raw"
    def authorEmail = "craig@quirk.biz"
    def description = '''\
The MongoFile plugin provides a MongoFileService that saves, retrieves and deletes files from a MongoDB file store. Furthermore, the domain classes have methods injected to handle these files.

Each file is stored in a MongoDB collection (bucket), named after the domain class name. 
'''
    def documentation = "https://github.com/quirklabs/grails-mongo-file/blob/master/README.md"
    def license = "APACHE"
    def organization = [ name: "Quirk Labs", url: "http://www.quirklabs.co.za" ]
    def developers = [[ name: "Juri Kuehn" ]]
    def issueManagement = [ system: "github", url: "https://github.com/quirklabs/grails-mongo-file/issues" ]
    def scm = [ url: "https://github.com/quirklabs/grails-mongo-file" ]

    def doWithDynamicMethods = { ctx ->
        for(domainClass in application.domainClasses) {
            domainClass.metaClass.mongoFileExists = { String fieldName = '' -> 
                getMongoFile(fieldName) != null
            }
            
            domainClass.metaClass.getMongoFile = { String fieldName = '' -> 
                def mongoFileService = ctx.getBean("mongoFileService")
                if(mongoFileService) {
                    return mongoFileService.getFile(delegate.getClass(), id, fieldName)
                }
                
                null
            }
            
            domainClass.metaClass.saveMongoFile = { org.springframework.web.multipart.MultipartFile file, String fieldName = '' -> 
                def mongoFileService = ctx.getBean("mongoFileService")
                if(mongoFileService) {
                    mongoFileService.saveFile(file, delegate.getClass(), id, fieldName)
                }
            }
            
            domainClass.metaClass.saveMongoFile = { byte[] fileContents, String fileName, String fieldName = '' -> 
                def mongoFileService = ctx.getBean("mongoFileService")
                if(mongoFileService) {
                    mongoFileService.saveFile(fileContents, fileName, delegate.getClass(), id, fieldName)
                }
            }       
            
            domainClass.metaClass.saveMongoFile = { InputStream inputStream, String fileName, String fieldName = '' -> 
                def mongoFileService = ctx.getBean("mongoFileService")
                if(mongoFileService) {
                    mongoFileService.saveFile(inputStream, fileName, delegate.getClass(), id, fieldName)
                }
            }                 
            
            domainClass.metaClass.deleteMongoFile = { String fieldName = '' -> 
                def mongoFileService = ctx.getBean("mongoFileService")
                if(mongoFileService) {
                    mongoFileService.deleteFile(delegate.getClass(), id, fieldName)
                }
            }
        }
    }
	
	def doWithSpring = {
		def mongoConfig = application.config.grails.mongo
		def mongo = new com.mongodb.Mongo(mongoConfig.host instanceof String ? mongoConfig.host : 'localhost', mongoConfig.port instanceof Integer ? mongoConfig.port : 27017)
		def credentials = new org.springframework.data.authentication.UserCredentials(mongoConfig.username instanceof String ? mongoConfig.username : '', mongoConfig.password instanceof String ? mongoConfig.password : '')
		
		mongoDbFactory(org.springframework.data.mongodb.core.SimpleMongoDbFactory, mongo, mongoConfig.databaseName instanceof String ? mongoConfig.databaseName : 'db', credentials)
	}
}
